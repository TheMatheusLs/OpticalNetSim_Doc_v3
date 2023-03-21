package RSA;

import java.util.ArrayList;
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
    /**
     * Armazena o valor do ciclo MSCL
     */
    private long cyclesMSCL;
    /**
     * Armazena o slots encontrado para cada rota
     */
    List<List<Integer>> slotsMSCL;

    public MSCLAlgorithm(){
        this.route = null;
        this.fSlots = new ArrayList<Integer>();
        this.slotsMSCL = new ArrayList<List<Integer>>();
        this.cyclesMSCL = 0;
    }

    
    public boolean findMSCLSequencial(List<Route> routeSolution, CallRequest callRequest) throws Exception {

        double valuesLostCapacity = Double.MAX_VALUE;
        int bestIndexMSCL = 0;

        for (Route currentRoute : routeSolution){

            if (currentRoute != null){
                valuesLostCapacity = getRouteMSCLCost(currentRoute, callRequest);
            }

            if (valuesLostCapacity < (Double.MAX_VALUE * 0.7)){
                this.route = routeSolution.get(bestIndexMSCL);
                this.fSlots = this.slotsMSCL.get(bestIndexMSCL);

                // Calcula o tamanho da requisição
                int reqNumbOfSlots = this.route.getReqSize(callRequest.getSelectedBitRate());

                callRequest.setReqNumbOfSlots(reqNumbOfSlots);

                return true;
            }
            
            bestIndexMSCL++;
        }
        
        this.route = null;
        this.fSlots = new ArrayList<Integer>();
        return false;
    }

    public boolean findMSCLCombinado(List<Route> routeSolution, CallRequest callRequest) throws Exception {

        List<Double> valuesLostCapacity = new ArrayList<Double>();
        int bestIndexMSCL = 0;
        int selectKRouteID = -1;

        for (Route currentRoute : routeSolution){

            if (currentRoute != null){
                valuesLostCapacity.add(getRouteMSCLCost(currentRoute, callRequest));
            } else {
                valuesLostCapacity.add(Double.MAX_VALUE * 0.9);
            }
        }

        double minValue = Double.MAX_VALUE * 0.7;

        for (int index = 0; index < routeSolution.size(); index++){

            if (minValue > valuesLostCapacity.get(index)){ // Se for igual, escolher a menor rota

                minValue = valuesLostCapacity.get(index);
                bestIndexMSCL = index;
                selectKRouteID = routeSolution.get(bestIndexMSCL).getkFindIndex();

            } else {

                if ((minValue == valuesLostCapacity.get(index)) && selectKRouteID > routeSolution.get(index).getkFindIndex()) { // Se for igual, escolher a menor rota
                    
                    minValue = valuesLostCapacity.get(index);
                    bestIndexMSCL = index;
                    selectKRouteID = routeSolution.get(bestIndexMSCL).getkFindIndex();
                }
            }
        }

        if (minValue < Double.MAX_VALUE * 0.5){
            this.route = routeSolution.get(bestIndexMSCL);
            this.fSlots = this.slotsMSCL.get(bestIndexMSCL);

            // Calcula o tamanho da requisição
            int reqNumbOfSlots = this.route.getReqSize(callRequest.getSelectedBitRate());
            callRequest.setReqNumbOfSlots(reqNumbOfSlots);
            return true;
        }

        return false;
    }

    private double getRouteMSCLCost(Route currentRoute, CallRequest callRequest) throws Exception{

        final int numberMaxSlotsPerLink = ParametersSimulation.getNumberOfSlotsPerLink();
        double bestLostCapacity = Double.MAX_VALUE * 0.8;

        // Calcula o tamanho da requisição
        int reqNumbOfSlots = currentRoute.getReqSize(callRequest.getSelectedBitRate());

        //FIXME: Encontrar todos os buracos para a rota principal capaz de alocar uma requisição de tamanho reqNumbOfSlots
        List<MSCLApeture> allAperturesInMainRoute = MSCLApeture.genApetures(currentRoute, 0, numberMaxSlotsPerLink - 1);

        // Verifica se é possível alocar essa requisção dentro da rota principal (route)
        boolean isPossibleToAlocateReq = false;
        for (MSCLApeture aperture : allAperturesInMainRoute){

            if (aperture.getSize() >= reqNumbOfSlots){
                isPossibleToAlocateReq = true;
                break;
            }
        }

        if (!isPossibleToAlocateReq){

            this.slotsMSCL.add(new ArrayList<Integer>());

            //Se FS não exite então Retorna um valor alto
            return Double.MAX_VALUE * 0.8; //Divide por 2 para evitar bugs do desempate
        }

        int bestSlotToCapacity = -1;

        // Percorre as lacunas da rota principal
        for (MSCLApeture apertureInMainRoute: allAperturesInMainRoute){
           

            final int initPosInApertureInMainRoute = apertureInMainRoute.getInitPosition();
            final int sizeInApertureInMainRoute = apertureInMainRoute.getSize();

            POINT_SLOT:for (int indexSlot = initPosInApertureInMainRoute; indexSlot < initPosInApertureInMainRoute + sizeInApertureInMainRoute; indexSlot++){

                int startSlot = indexSlot;
                int finalSlot = indexSlot + reqNumbOfSlots - 1;

                if (finalSlot >= numberMaxSlotsPerLink){
                    break;
                }

                // Verifica se é possível alocar a requisição iniciando em startSlot 
                POINT_TEST:for (int slot = startSlot; slot <= finalSlot; slot++){

                    if (currentRoute.getSlotValue(slot) > 0){
                        continue POINT_SLOT;
                    }
                    else {
                        continue POINT_TEST;
                    }
                }

                //* Chegando aqui é possível fazer a alocação e começa o cálculo de capacidade para o slot

                // Cria uma lista dos slots fake para a requisição
                List<Integer> slotsReqFake = new ArrayList<Integer>();
                for (int s = startSlot; s <= finalSlot; s++){
                    slotsReqFake.add(s);
                }

                double lostCapacityTotal = 0.0;

                // *** Rota principal

                // ** Cálculo da capacidade antes da alocação na rota principal
                double capacityBeforeRoute = 0.0;

                int[] possibleSlotsByRoute = currentRoute.getAllReqSizes();

                for (int possibleReqSize: possibleSlotsByRoute){

                    if (possibleReqSize > sizeInApertureInMainRoute){
                        break;
                    }
                    capacityBeforeRoute += (sizeInApertureInMainRoute - possibleReqSize + 1);
                }

                // Aloca a requisição fake
                currentRoute.incrementSlotsOcupy(slotsReqFake);

                // ** Cálculo da capacidade depois da alocação na rota principal

                //Encontra os buracos formados após a alocação fake. FIXME: Verificar se os parâmetros informados a busca dos burracos estão corretos. Seria todos os buracos formados ou somente os laterais?
                List<MSCLApeture> aperturesInMainRouteAfter = MSCLApeture.genApetures(currentRoute, initPosInApertureInMainRoute, initPosInApertureInMainRoute + sizeInApertureInMainRoute - 1);

                double capacityAfterRoute = 0.0;

                for (MSCLApeture apetureInApetureMainRouteAfter : aperturesInMainRouteAfter) {

                    int sizeInapetureInApetureMainRouteAfter = apetureInApetureMainRouteAfter.getSize();

                    for (int possibleReqSize: possibleSlotsByRoute){

                        if (possibleReqSize > sizeInapetureInApetureMainRouteAfter){
                            break;
                        }
                        capacityAfterRoute += (sizeInapetureInApetureMainRouteAfter - possibleReqSize + 1);
                    }
                }

                currentRoute.decreasesSlotsOcupy(slotsReqFake);

                // Calcula a perda de capacidade na rota principal
                lostCapacityTotal += capacityBeforeRoute - capacityAfterRoute;


                /// *** Rotas interferentes

                List<Route> allConflictRoutes = currentRoute.getAllConflictRoutes();
                //TODO: Implementar os demais modos de rotas interferentes

                //TODO: Implementar os demais modos de ordenação das rotas interferentes

                for (Route conflictRoute: allConflictRoutes){

                    // Incrementa uma unidade nos ciclos MSCL
                    this.cyclesMSCL++;

                    // Busca o mínimo a esquerda. TODO: Entender essa implementação
                    int minSlot = this.findSlotInLeft(conflictRoute, startSlot);

                    // Busca o máximo a direita. TODO: Entender essa implementação
                    int maxSlot = this.findSlotInRight(conflictRoute, finalSlot);

                    List<MSCLApeture> allApeturesAfectInConflictRoute = MSCLApeture.genApetures(conflictRoute, minSlot, maxSlot);

                    // ** Cálculo da capacidade antes da alocação na rota interferente

                    capacityBeforeRoute = 0.0;    

                    for (MSCLApeture apetureInConflictRoute : allApeturesAfectInConflictRoute) {

                        int sizeInApetureInConflictRoute = apetureInConflictRoute.getSize();

                        for (int possibleReqSize: possibleSlotsByRoute){

                            if (possibleReqSize > sizeInApetureInConflictRoute){
                                break;
                            }
                            capacityBeforeRoute += (sizeInApetureInConflictRoute - possibleReqSize + 1);
                        }
                    }

                    // Aloca a requisição fake
                    conflictRoute.incrementSlotsOcupy(slotsReqFake);

                    // ** Cálculo da capacidade depois da alocação na rota principal

                    //Encontra os buracos formados
                    List<MSCLApeture> allApertureConflictRoute = MSCLApeture.genApetures(conflictRoute, minSlot, maxSlot);

                    capacityAfterRoute = 0.0;
                    for (MSCLApeture apetureInConflictRoute : allApertureConflictRoute) {

                        int sizeApetureInConflictRoute = apetureInConflictRoute.getSize();

                        for (int possibleReqSize: possibleSlotsByRoute){

                            if (possibleReqSize > sizeApetureInConflictRoute){
                                break;
                            }
                            capacityAfterRoute += (sizeApetureInConflictRoute - possibleReqSize + 1);
                        }
                    }

                    conflictRoute.decreasesSlotsOcupy(slotsReqFake);

                    if (capacityBeforeRoute < capacityAfterRoute){
                        throw new Exception("Erro: emptySlots = -1");
                    }

                    lostCapacityTotal += capacityBeforeRoute - capacityAfterRoute;
                }

                if (lostCapacityTotal < 0){
                    throw new Exception("Erro: emptySlots = -1");
                }

                if (lostCapacityTotal < bestLostCapacity){
                    bestLostCapacity = lostCapacityTotal;
                    bestSlotToCapacity = indexSlot;
                }
            }
        }

        List<Integer> slotsReq = new ArrayList<Integer>();
        for (int s = bestSlotToCapacity; s <= bestSlotToCapacity + reqNumbOfSlots - 1; s++){
            slotsReq.add(s);
        }

        this.slotsMSCL.add(slotsReq);

        return bestLostCapacity;
    }

    public int findSlotInLeft(Route route, int initSlot) throws Exception{
        // Busca o mínimo a esquerda
        int minSlot = initSlot; // Força um erro
        for (int min = initSlot; min >= 0;min--){

            //this.cycles++;

            if (route.getSlotValue(min) == 0){
                minSlot = min;
            } else {
                break;
            }
        }

        if (minSlot == -1){
            throw new Exception("Erro: emptySlots = -1");
        }

        return minSlot;
    }

    public int findSlotInRight(Route route, int initSlot) throws Exception{
        // Busca o máximo a direita
        int maxSlot = initSlot; // Força um erro
        for (int max = initSlot; max < ParametersSimulation.getNumberOfSlotsPerLink(); max++){

            //this.cycles++;

            if (route.getSlotValue(max) == 0){
                maxSlot = max;
            } else {
                break;
            }
        }

        if (maxSlot == ParametersSimulation.getNumberOfSlotsPerLink() + 1){
            throw new Exception("Erro: emptySlots = -1");
        }

        return maxSlot;
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
