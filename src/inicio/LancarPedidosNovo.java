package inicio;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import save.salvarDadosEmpenho;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LancarPedidosNovo implements AcaoRotinaJava {

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

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        for (Registro linha : linhas) {
            numContrato = (BigDecimal) linha.getCampo("NUMCONTRATO");
            BigDecimal codConEmp = (BigDecimal) linha.getCampo("CODCONEMP");
            codProd = (BigDecimal) linha.getCampo("CODPROD");
            qtdLiberar = (BigDecimal) linha.getCampo("QTDLIBERAR");
            qtdDisponivel = (BigDecimal) linha.getCampo("QTDDISPONIVEL");
            String empenho = (String) linha.getCampo("EMPENHO");
            if (nuNota == null) nuNota = BigDecimal.ZERO;

                if (qtdLiberar.compareTo(qtdDisponivel) < 0) {

                    if (qtdLiberar.compareTo(BigDecimal.ZERO) > 0) {

                        final String sql2 = consultasDados.retornaDadosCabecalho(numContrato.toString());
                        PreparedStatement pstmt = jdbcWrapper.getPreparedStatement(sql2);
                        ResultSet rs = pstmt.executeQuery();

                        if (rs.next()) {
                            BigDecimal codEmp = rs.getBigDecimal("CODEMP");
                            BigDecimal codParc = rs.getBigDecimal("CODPARC");
                            //BigDecimal codTipOper = new BigDecimal(codTipOper);
                            BigDecimal codTipVenda = rs.getBigDecimal("CODTIPVENDA");
                            BigDecimal codNat = rs.getBigDecimal("CODNAT");
                            BigDecimal codCencus = rs.getBigDecimal("CODCENCUS");
                            BigDecimal codProj = rs.getBigDecimal("CODPROJ");

                            if (!(nuNota.intValue() > 0)) {

                                nuNota = salvarDadosEmpenho.salvarCabecalhoDados(
                                        dwf,
                                        arg0,
                                        codEmp,
                                        codParc,
                                        new BigDecimal(codTipOper),
                                        codTipVenda,
                                        codNat,
                                        codCencus,
                                        codProj,
                                        BigDecimal.ZERO,
                                        numContrato,
                                        empenho);
                            }

                            String consultaItens = consultasDados.retornaDadosItensPedidos(codProd.toString(), numContrato.toString());
                            pstmt = jdbcWrapper.getPreparedStatement(consultaItens);
                            rs = pstmt.executeQuery();

                            while (rs.next()) {

                                String codVol = rs.getString("CODVOL");
                                BigDecimal vlrUnit = rs.getBigDecimal("VLRUNIT");
                                BigDecimal vlrTot = vlrUnit.multiply(qtdLiberar);

                                salvarDadosEmpenho.salvarItensDados(
                                        dwf,
                                        codEmp,
                                        nuNota,
                                        codProd,
                                        qtdLiberar,
                                        codVol,
                                        vlrUnit,
                                        vlrTot);

                                final String update1 = "UPDATE AD_CONVERTEREMPENHO SET QTDLIBERAR = 0,QTDDISPONIVEL = QTDDISPONIVEL-" +qtdLiberar+ "  WHERE NUMCONTRATO = " + numContrato + " AND CODPROD = " + codProd;
                                pstmt = jdbcWrapper.getPreparedStatement(update1);
                                pstmt.executeUpdate();
                            }
                            //empenhoFuncionalidades.liberarEmpenho(arg0, new BigDecimal(numContrato), empenho);
                        }

                    } else {
                        arg0.mostraErro("Quantidade à liberar deve ser maior que zero!");
                    }
                } else {
                    arg0.mostraErro("Quantidade digitada não pode ser maior que a disponivel! Cód. Produto : " + codProd);
                }

                arg0.setMensagemRetorno("Pedido " + nuNota + " gerado com sucesso");
        }
    }
}