package inicio;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.CentralFinanceiro;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.comercial.processors.EnvironmentProcessor;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import helpper.CabecalhoNota;
import helpper.Empenho;
import helpper.ItemNota;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class LancaPedidosNew implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao arg0) throws Exception {

        Registro[] linhas = arg0.getLinhas();
        BigDecimal numContrato;
        BigDecimal nuNota = null;
        String codTipOper = (String) arg0.getParam("TIPOPEDIDO");
        Set<String> empenhos = new HashSet<>();
        Set<String> tipo = new HashSet<>();
        BigDecimal codVend = ComercialUtils.getVendedorUsuLogado(AuthenticationInfo.getCurrent().getUserID());

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        numContrato = (BigDecimal) Arrays.stream(linhas).findFirst().get().getCampo("NUMCONTRATO");
        DynamicVO contratoVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CONTRATO, numContrato);


        Collection<DynamicVO> empenhosLiberados = dwf.findByDynamicFinderAsVO(new FinderWrapper("AD_CONVERTEREMPENHO", "this.QTDLIBERAR > 0 AND this.CODVEND = 9 AND this.NUMCONTRATO = ?", new Object[] {numContrato}));
        empenhosLiberados.forEach(vo -> empenhos.add(vo.asString("EMPENHO")));
        empenhosLiberados.forEach(vo -> tipo.add(vo.asString("TIPO")));
        if (empenhos.size() > 1) arg0.mostraErro("Mais de um empenho selecionado para o mesmo pedido.");
        if (tipo.size() > 1) arg0.mostraErro("Acessorios e itens nao podem estar no mesmo pedido.");

        DynamicVO cabVO = CabecalhoNota.getOne("this.CODTIPOPER = 1005 AND this.NUMCONTRATO = ?", new Object[] {numContrato});
        if (cabVO == null) throw new MGEModelException("Não foi encontrado nota do contrato.");


        nuNota = Empenho.salvaCabecalhoNota(
                dwf,
                contratoVO.asBigDecimalOrZero("CODEMP"),
                contratoVO.asBigDecimalOrZero("CODPARC"),
                new BigDecimal(codTipOper),
                contratoVO.asBigDecimalOrZero("CODTIPVENDA"),
                contratoVO.asBigDecimalOrZero("CODNAT"),
                contratoVO.asBigDecimalOrZero("CODCENCUS"),
                contratoVO.asBigDecimalOrZero("CODPROJ"),
                codVend,
                BigDecimal.ZERO,
                numContrato,
                empenhos.stream().findFirst().get());


        for (DynamicVO empenhoVO : empenhosLiberados) {


            DynamicVO item = ItemNota.getOne("this.NUNOTA = ? AND CODPROD = ? AND CODVOL = ?",
                    new Object[] {cabVO.asBigDecimalOrZero("NUNOTA"), empenhoVO.asBigDecimalOrZero("CODPROD"), empenhoVO.asString("CODVOL")});


            if (item != null) {


                String codVol = empenhoVO.asString("CODVOL");
                BigDecimal qtdLiberar = empenhoVO.asBigDecimalOrZero("QTDLIBERAR");
                BigDecimal vlrUnit = item.asBigDecimalOrZero("VLRUNIT");
                BigDecimal vlrTot = vlrUnit.multiply(qtdLiberar);

                final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = " + empenhoVO.asBigDecimalOrZero("CODPROD").toString() + " and CODVOL = '" + codVol + "'";
                PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
                    BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

                    if (divideOuMultiplica.equalsIgnoreCase("M")) {
                        qtdLiberar = qtdLiberar.multiply(quantidade);
                        vlrUnit = vlrUnit.divide(quantidade, MathContext.DECIMAL128);

                    } else if (divideOuMultiplica.equalsIgnoreCase("D")) {
                        qtdLiberar = qtdLiberar.divide(quantidade, MathContext.DECIMAL128);
                        vlrUnit = vlrUnit.multiply(quantidade);
                    }
                    vlrTot = vlrUnit.multiply(qtdLiberar);
                }

                // Verifca ATUALEST da TOP para alterar itens
                DynamicVO topVO = TipoOperacaoUtils.getTopVO(BigDecimal.valueOf(Integer.parseInt(codTipOper)));
                String atualEst = (String) topVO.getProperty("ATUALEST");

                Empenho.salvaItemNota(
                        dwf,
                        contratoVO.asBigDecimal("CODEMP"),
                        nuNota,
                        empenhoVO.asBigDecimalOrZero("CODPROD"),
                        qtdLiberar,
                        codVol,
                        vlrUnit,
                        vlrTot,
                        atualEst);
            }

            empenhoVO.setProperty("QTDLIBERAR", BigDecimal.ZERO);
            empenhoVO.setProperty("QTDDISPONIVEL", empenhoVO.asBigDecimalOrZero("QTDDISPONIVEL").subtract(empenhoVO.asBigDecimalOrZero("QTDLIBERAR")));

            dwf.saveEntity("AD_CONVERTEREMPENHO", (EntityVO) empenhoVO);


        }

        final ImpostosHelpper impostosHelper = new ImpostosHelpper();
        impostosHelper.calcularImpostos(nuNota);
        impostosHelper.totalizarNota(nuNota);

        final CentralFinanceiro centralFinanceiro = new CentralFinanceiro();
        centralFinanceiro.inicializaNota(nuNota);
        centralFinanceiro.refazerFinanceiro();

        arg0.setMensagemRetorno("Pedido " + nuNota + " gerado com sucesso");
    }
}