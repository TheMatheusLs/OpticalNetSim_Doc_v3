package RSA;

import java.util.List;

import CallRequests.CallRequest;
import Config.ParametersSimulation;
import RSA.Routing.Route;

public class MSCLAlgorithm {
    
    /**
     * Rota selecionada pelo algoritmo de roteamento
     */
    private Route route;
    /**
     * Conjunto de slots selecionados pelo algoritmo de alocação do espectro
     */
    private List<Integer> fSlots;

    private long cyclesMSCL;

    public MSCLAlgorithm(){
        this.cyclesMSCL = 0;
    }

    private double getRouteMSCLCost(Route currentRoute, CallRequest callRequest) throws Exception{

        final int numberMaxSlotsPerLink = ParametersSimulation.getNumberOfSlotsPerLink();
        double bestLostCapacity = Double.MAX_VALUE;

        // Calcula o tamanho da requisição
        int reqNumbOfSlots = currentRoute.getReqSize(callRequest.getSelectedBitRate());

        //FIXME: Encontrar todos os buracos para a rota principal capaz de alocar uma requisição de tamanho reqNumbOfSlots
        List<MSCLApeture> allApertures = MSCLApeture.genApetures(route, 0, numberMaxSlotsPerLink - 1);




        return bestLostCapacity;
    }




    /**
     * Método para retornar a rota encontrada após utilizar o algoritmo de roteamento.
     * 
     * @return Retorna a rota encontrada
     */
    public Route getRoute() {
        return this.route;
    }

    /**
     * Método para retornar o conjunto dos slots encontrado
     * 
     * @return O conjunto dos slots encontrado
     */
    public List<Integer> getSlotsIndex() {
        return this.fSlots;
    }

    /**
     * Método para retornar o valor do ciclosMSCL
     * 
     * @return O valor do CicloMSCL para a rota selecionada
     */
    public long getCyclesMSCL() {
        return this.cyclesMSCL;
    }
}
