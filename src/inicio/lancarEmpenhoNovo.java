package inicio;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.Finder;
import consultas.consultasDados;
import save.salvarDadosEmpenho;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedList;

/**
 * 
 */
public class lancarEmpenhoNovo implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao arg0) throws Exception {
        Registro[] linhas = arg0.getLinhas();
        BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();

        //String qtdLiberar = ""+arg0.getParam("QUANTIDADE");
        String empenho = (String) arg0.getParam("EMPENHO");

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        BigDecimal qtdDisponivel;
        BigDecimal numContrato;
        BigDecimal codProd;
        BigDecimal qtdLiberar;

        for (Registro linha: linhas) {
            //qtdDisponivel = (BigDecimal) linha.getCampo("QTDDISPONIVEL");
            //codProd = (BigDecimal) linha.getCampo("CODPROD");
            //qtdLiberar = (BigDecimal) linha.getCampo("QTDLIBERAR");
            numContrato = BigDecimal.valueOf((Long) linha.getCampo("NUMCONTRATO"));

            LinkedList<DynamicVO> itensEmpenhoVO = (LinkedList<DynamicVO>) EntityFacadeFactory.getDWFFacade().findByDynamicFinder(new FinderWrapper("AD_ITENSEMPENHO", "this.QTDLIBERAR > 0 AND this.NUMCONTRATO = ?", new Object[] { numContrato }));

            for (DynamicVO itemVO: itensEmpenhoVO) {

                qtdDisponivel = itemVO.asBigDecimalOrZero("QTDDISPONIVEL");
                codProd = itemVO.asBigDecimalOrZero("CODPROD");
                qtdLiberar = itemVO.asBigDecimalOrZero("QTDLIBERAR");
                final boolean qtdLiberarMenorQueDisponivel = qtdLiberar.compareTo(qtdDisponivel) < 0;

                if (qtdLiberarMenorQueDisponivel) {

                    String consulta = consultasDados.retornoValidaEmpenho();
                    PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(consulta);
                    ResultSet rs = pstmt.executeQuery();

                    final boolean temPermissao = EntityFacadeFactory.getDWFFacade().findByDynamicFinder(new FinderWrapper(DynamicEntityNames.USUARIO, "this.AD_LEBERAEMPENHO = 'S' AND this.CODUSU = ?", new Object[] { codUsuarioLogado })).size() > 0;

                    if (temPermissao) {

                        DynamicVO contratoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CONTRATO, numContrato);

                        BigDecimal codParc = contratoVO.asBigDecimalOrZero("CODPARC");
                        BigDecimal codTipOper = contratoVO.asBigDecimalOrZero("AD_CODTIPOPER");
                        BigDecimal codEmp = contratoVO.asBigDecimalOrZero("CODEMP");
                        BigDecimal codTipVenda = contratoVO.asBigDecimalOrZero("CODTIPVENDA");
                        BigDecimal codNat = contratoVO.asBigDecimalOrZero("CODNAT");
                        BigDecimal codCenCus = contratoVO.asBigDecimalOrZero("CODCENCUS");
                        BigDecimal codProj = contratoVO.asBigDecimalOrZero("CODPROJ");

                                /*BigDecimal nuNota = salvarDadosEmpenho.salvarCabecalhoDados(
                                         dwf,
                                         arg0,
                                        codEmp,
                                        codParc,
                                        codTipOper,
                                        codTipVenda,
                                        codNat,
                                        codCenCus,
                                        codProj,
                                        new BigDecimal(0),
                                        numContrato,
                                        empenho);*/


                        final String sql = consultasDados.retornaDadosItens(codProd.toString(), numContrato.toString());
                        pstmt = jdbcWrapper.getPreparedStatement(sql);
                        rs = pstmt.executeQuery();
                        while (rs.next()) {

                            String codVol = rs.getString("CODVOL");
                            BigDecimal vlrUnit = rs.getBigDecimal("VLRUNIT");
                            BigDecimal vlrTot = vlrUnit.multiply(qtdLiberar);

                                        /*salvarDadosEmpenho.salvarItensDados(
                                        dwf,
                                        arg0,
                                        codEmp,
                                        nuNota,
                                        new BigDecimal(codProd),
                                        new BigDecimal(qtdLiberar),
                                        codVol,
                                        new BigDecimal(vlrUnit),
                                        vlrTot);*/
                            salvarDadosEmpenho.gerarEmpenhoConverter(dwf, codProd, numContrato, qtdLiberar, qtdLiberar, empenho);

                            final String update = "UPDATE TCSPSC set AD_QTDLIBERAR=AD_QTDLIBERAR-" + qtdLiberar + "  WHERE NUMCONTRATO = " + numContrato + " AND CODPROD = " + codProd;
                            pstmt = jdbcWrapper.getPreparedStatement(update);
                            pstmt.executeUpdate();

                            final String update1 = "UPDATE AD_ITENSEMPENHO set QTDLIBERAR = 0, AD_DISPONIVEL = AD_DISPONIVEL-" + qtdLiberar + "  WHERE NUMCONTRATO = " + numContrato + " AND CODPROD = " + codProd;
                            pstmt = jdbcWrapper.getPreparedStatement(update1);
                            pstmt.executeUpdate();

                        }

                            //empenhoFuncionalidades.liberarEmpenho(arg0, new BigDecimal(numContrato), empenho);

                            /*EntityFacade dwf,
                            ContextoAcao arg0,
                            BigDecimal codEmp,
                            BigDecimal codParc,
                            BigDecimal codTipOper,
                            BigDecimal codTipVenda,
                            BigDecimal codNat,
                            BigDecimal codCenCus,
                            BigDecimal codProj,
                            BigDecimal vlrNota*/

                    } else {
                        arg0.mostraErro("Usuário não tem permissão");
                    }

                } else {
                    arg0.mostraErro("Quantidade a liberar não pode ser zero ou maior que a quantidade disponível.");

                }



            } //Fim da iteração dos itens do empenho



        } // Fim da iteração das linhas

        arg0.setMensagemRetorno("Empenho liberado com sucesso");
    } // Fim da ação
}
