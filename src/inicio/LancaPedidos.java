package inicio;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.CentralFinanceiro;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import helpper.Empenho;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LancaPedidos implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao arg0) throws Exception {

        Registro[] linhas = arg0.getLinhas();
        BigDecimal qtdDisponivel;
        BigDecimal numContrato;
        BigDecimal codProd;
        BigDecimal qtdLiberar;
        BigDecimal nuNota = null;
        //BigDecimal qtd = BigDecimal.valueOf((Long) arg0.getParam("QUANTIDADE"));
        String codTipOper = (String) arg0.getParam("TIPOPEDIDO");
        BigDecimal codVend = ComercialUtils.getVendedorUsuLogado(AuthenticationInfo.getCurrent().getUserID());


        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        for (Registro linha : linhas) {
            numContrato = (BigDecimal) linha.getCampo("NUMCONTRATO");
            BigDecimal codConEmp = (BigDecimal) linha.getCampo("CODCONEMP");
            BigDecimal nuNotaContrato = (BigDecimal) linha.getCampo("NUNOTA");
            BigDecimal sequencia = (BigDecimal) linha.getCampo("SEQUENCIA");
            codProd = (BigDecimal) linha.getCampo("CODPROD");
            qtdLiberar = (BigDecimal) linha.getCampo("QTDLIBERAR");
            qtdDisponivel = (BigDecimal) linha.getCampo("QTDDISPONIVEL");
            String empenho = (String) linha.getCampo("EMPENHO");
            String codVol = (String) linha.getCampo("CODVOL");
            if (nuNota == null) nuNota = BigDecimal.ZERO;

                if (qtdLiberar.compareTo(qtdDisponivel) <= 0) {

                    if (qtdLiberar.compareTo(BigDecimal.ZERO) > 0) {

                        final String sql2 = consultasDados.retornaDadosCabecalho(numContrato.toString());
                        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sql2);
                        ResultSet rs = pstmt.executeQuery();

                        if (rs.next()) {
                            BigDecimal codEmp = rs.getBigDecimal("CODEMP");
                            BigDecimal codParc = rs.getBigDecimal("CODPARC");
                            BigDecimal codTipVenda = rs.getBigDecimal("CODTIPVENDA");
                            BigDecimal codNat = rs.getBigDecimal("CODNAT");
                            BigDecimal codCencus = rs.getBigDecimal("CODCENCUS");
                            BigDecimal codProj = rs.getBigDecimal("CODPROJ");

                            if (!(nuNota.intValue() > 0)) {

                                nuNota = Empenho.salvaCabecalhoNota(
                                        dwf,
                                        codEmp,
                                        codParc,
                                        new BigDecimal(codTipOper),
                                        codTipVenda,
                                        codNat,
                                        codCencus,
                                        codProj,
                                        codVend,
                                        BigDecimal.ZERO,
                                        numContrato,
                                        empenho);
                            }


                            pstmt = jdbcWrapper.getPreparedStatement("UPDATE AD_CONVERTEREMPENHO SET QTDLIBERAR = 0, QTDDISPONIVEL = QTDDISPONIVEL-" +qtdLiberar+ "  WHERE NUMCONTRATO = " + numContrato + " AND CODCONEMP = " + codConEmp);
                            pstmt.executeUpdate();

                            String consultaItens = consultasDados.retornaDadosItensPedidos(codProd.toString(), numContrato.toString(), codVol);
                            pstmt = jdbcWrapper.getPreparedStatement(consultaItens);
                            rs = pstmt.executeQuery();

                            while (rs.next()) {

                                codVol = rs.getString("CODVOL");
                                BigDecimal vlrUnit = rs.getBigDecimal("VLRUNIT");
                                BigDecimal vlrTot = vlrUnit.multiply(qtdLiberar);

                                final String sqlunidade = "SELECT DIVIDEMULTIPLICA,MULTIPVLR,QUANTIDADE FROM TGFVOA WHERE CODPROD = "+codProd+" and CODVOL = '"+codVol+"'";
                                pstmt = jdbcWrapper.getPreparedStatement(sqlunidade);
                                rs = pstmt.executeQuery();
                                if (rs.next()){
                                    final String divideOuMultiplica = rs.getString("DIVIDEMULTIPLICA");
                                    BigDecimal quantidade = rs.getBigDecimal("QUANTIDADE").multiply(rs.getBigDecimal("MULTIPVLR"));

                                    if (divideOuMultiplica.equalsIgnoreCase("M")) {
                                        qtdLiberar = qtdLiberar.multiply(quantidade);
                                        vlrUnit = vlrUnit.divide(quantidade, MathContext.DECIMAL128);

                                    }
                                    else if (divideOuMultiplica.equalsIgnoreCase("D")) {
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
                                        codEmp,
                                        nuNota,
                                        codProd,
                                        qtdLiberar,
                                        codVol,
                                        vlrUnit,
                                        vlrTot,
                                        atualEst);
                            }
                            //empenhoFuncionalidades.liberarEmpenho(arg0, new BigDecimal(numContrato), empenho);
                        }



                    } else {
                        arg0.mostraErro("Quantidade à liberar deve ser maior que zero!");
                    }
                } else {
                    arg0.mostraErro("Quantidade digitada não pode ser maior que a disponivel ! Cód. Produto : " + codProd);
            }




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