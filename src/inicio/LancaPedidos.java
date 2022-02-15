package inicio;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.CentralFinanceiro;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import helpper.Impostos;

import java.math.BigDecimal;
import java.util.*;

import static helpper.Empenho.geraPedido;

/**
 * Lançamento de pedidos verificando todas as linhas na tela
 * Para usuários que tem permissão de lançar pedidos para outros vendedores o parâmtro CODVEND é necessário
 */

public class LancaPedidos implements AcaoRotinaJava {

    final static private BigDecimal codUsuLogado = AuthenticationInfo.getCurrent().getUserID();

    @Override
    public void doAction(ContextoAcao arg0) throws Exception {

        Registro[] linhas = arg0.getLinhas();

        final boolean acessoTotal = "S".equals(((DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.USUARIO, codUsuLogado)).asString("AD_ACESSOCON"));
        final BigDecimal codVendLogado = ComercialUtils.getVendedorUsuLogado(codUsuLogado);

        // Se parametro do Vendedor for nulo, pega o vendedor do usuário logado.
        BigDecimal codVend = arg0.getParam("CODVEND") == null ? ComercialUtils.getVendedorUsuLogado(codUsuLogado) : new BigDecimal((String) arg0.getParam("CODVEND"));

        if (!acessoTotal) codVend = codVendLogado;

        BigDecimal numContrato = (BigDecimal) Arrays.stream(linhas).findFirst().get().getCampo("NUMCONTRATO");
        DynamicVO contratoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CONTRATO, numContrato);

        final BigDecimal nuNota = geraPedido(arg0, codVend, contratoVO);

        final ImpostosHelpper impostosHelper = new ImpostosHelpper();
        impostosHelper.calcularImpostos(nuNota);
        impostosHelper.totalizarNota(nuNota);

        final CentralFinanceiro centralFinanceiro = new CentralFinanceiro();
        centralFinanceiro.inicializaNota(nuNota);
        centralFinanceiro.refazerFinanceiro();

        arg0.setMensagemRetorno("Pedido " + nuNota + " gerado com sucesso");
    }


}