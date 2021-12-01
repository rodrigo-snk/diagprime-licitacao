package inicio;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import consultas.consultasDados;
import save.salvarDadosEmpenho;

public class lancarEmpenho implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao arg0) throws Exception {
        //String qtd = ""+arg0.getParam("QUANTIDADE");
        String empenho = "" + arg0.getParam("EMPENHO");

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        Registro[] linhas = arg0.getLinhas();
        String qtdDisponivel = "0";
        String numContrato = "0";
        String codProd = "0";
        String qtd = "0";
        //String empenho = "";


        for (Integer i = 0; i < linhas.length; i++) {
            //qtdDisponivel = "" + linhas[i].getCampo("QTDDISPONIVEL");
            numContrato = "" + linhas[i].getCampo("NUMCONTRATO");
            //	codProd = "" + linhas[i].getCampo("CODPROD");
            //	qtd = "" + linhas[i].getCampo("QTDLIBERAR");

            String sql = "SELECT QTDDISPONIVEL,NUMCONTRATO,CODPROD,QTDLIBERAR FROM AD_ITENSEMPENHO WHERE  QTDLIBERAR>0 AND NUMCONTRATO = " + numContrato;
            PreparedStatement updateValidando = jdbcWrapper.getPreparedStatement(sql);
            ResultSet consultaValidando = updateValidando.executeQuery();

            while (consultaValidando.next()) {

                qtdDisponivel = consultaValidando.getString("QTDDISPONIVEL");
                codProd = consultaValidando.getString("CODPROD");
                qtd = consultaValidando.getString("QTDLIBERAR");

                //empenho = "" + linhas[i].getCampo("EMPENHO");

                if (qtdDisponivel.equalsIgnoreCase("null")) {
                    qtdDisponivel = "0";
                }
                //arg0.mostraErro(" qtdDisponivel "+qtdDisponivel+" - "+qtd);

                //arg0.mostraErro(qtdDisponivel+" - "+qtd);
                if (!(Integer.parseInt(qtd) > Integer.parseInt(qtdDisponivel))) {

                    if (Integer.parseInt(qtd) > 0) {

                        String consulta = consultasDados.validaEmpenho();
                        PreparedStatement updateValidando1 = jdbcWrapper.getPreparedStatement(consulta);
                        ResultSet consultaValidando1 = updateValidando1.executeQuery();

                        if (consultaValidando1.next()) {

                            String consultaCabec = consultasDados.retornaDadosCabecalho(numContrato);
                            PreparedStatement preCabecalho = jdbcWrapper.getPreparedStatement(consultaCabec);
                            ResultSet consultaPre = preCabecalho.executeQuery();

                            if (consultaPre.next()) {

                                BigDecimal codEmp = consultaPre.getBigDecimal("CODEMP");
                                BigDecimal codParc = consultaPre.getBigDecimal("CODPARC");
                                BigDecimal codTipOper = consultaPre.getBigDecimal("AD_CODTIPOPER");
                                BigDecimal codTipVenda = consultaPre.getBigDecimal("CODTIPVENDA");
                                BigDecimal codNat = consultaPre.getBigDecimal("CODNAT");
                                BigDecimal codCencus = consultaPre.getBigDecimal("CODCENCUS");
                                BigDecimal codProj = consultaPre.getBigDecimal("CODPROJ");

                                /*BigDecimal nuNota = salvarDadosEmpenho.salvarCabecalhoDados(
                                         dwf,
                                         arg0,
                                        codEmp,
                                        codParc,
                                        codTipOper,
                                        codTipVenda,
                                        codNat,
                                        codCencus,
                                        codProj,
                                        new BigDecimal(0),
                                        numContrato,
                                        empenho);*/


                                String consultaItens = consultasDados.retornaDadosItens(codProd, numContrato);
                                PreparedStatement preItens = jdbcWrapper.getPreparedStatement(consultaItens);
                                ResultSet consultaPreItens = preItens.executeQuery();
                                while (consultaPreItens.next()) {

                                    String codVol = consultaPreItens.getString("CODVOL");
                                    BigDecimal vlrUnit = consultaPreItens.getBigDecimal("VLRUNIT");
                                    BigDecimal vlrTot = vlrUnit.multiply(new BigDecimal(qtd));
	  	    			
                                    /*salvarDadosEmpenho.salvarItensDados(
                                    dwf,
                                    arg0,
                                    codEmp,
                                    nuNota,
                                    new BigDecimal(codProd),
                                    new BigDecimal(qtd),
                                    codVol,
                                    new BigDecimal(vlrUnit),
                                    vlrTot);*/

                                    salvarDadosEmpenho.gerarEmpenhoConverter(
                                            dwf,
                                            new BigDecimal(codProd),
                                            codVol,
                                            new BigDecimal(numContrato),
                                            new BigDecimal(qtd),
                                            new BigDecimal(qtd),
                                            empenho);

                                    String update = "UPDATE TCSPSC set AD_QTDLIBERAR=AD_QTDLIBERAR-" + qtd + "  WHERE NUMCONTRATO = " + numContrato + " AND CODPROD = " + codProd;
                                    PreparedStatement preUpdt = jdbcWrapper.getPreparedStatement(update);
                                    preUpdt.executeUpdate();

                                    String update1 = "UPDATE AD_ITENSEMPENHO set QTDLIBERAR=0,AD_DISPONIVEL=AD_DISPONIVEL-" + qtd + "  WHERE NUMCONTRATO = " + numContrato + " AND CODPROD = " + codProd;
                                    PreparedStatement preUpdt1 = jdbcWrapper.getPreparedStatement(update1);
                                    preUpdt1.executeUpdate();


                                }

                                //empenhoFuncionalidades.liberarEmpenho(arg0, new BigDecimal(numContrato), empenho);
                            }
                            /*EntityFacade dwf,
                            ContextoAcao arg0,
                            BigDecimal codEmp,
                            BigDecimal codParc,
                            BigDecimal codTipOper,
                            BigDecimal codTipVenda,
                            BigDecimal codNat,
                            BigDecimal codCencus,
                            BigDecimal codProj,
                            BigDecimal vlrNota*/

                        } else {
                            arg0.mostraErro("Usuário não tem permissão");
                        }
                    } else {
                        arg0.mostraErro("Obrigatório, quantidade ser maior que zero !");
                    }
                } else {
                    arg0.mostraErro("Quantidade digitada não pode ser maior que a disponivel ! Cód. Prod :" + codProd);
                }
            }
        }
        arg0.setMensagemRetorno("Empenho liberado com sucesso");
    }


}
