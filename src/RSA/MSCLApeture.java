package RSA;

import java.util.ArrayList;
import java.util.List;

import RSA.Routing.Route;

public class MSCLApeture {
    /**
     * Posição inicial da lacuna
     */
    private int initPosition;
    /**
     * Tamanho da lacuna em slots
     */
    private int size;

    public MSCLApeture(int initPosition, int size) {
        this.initPosition = initPosition;
        this.size = size;
    }

    public int getInitPosition() {
        return initPosition;
    }
    
    public int getSize() {
        return size;
    } 

    /**
     * Retorna uma lista de lacunas 
     * 
     * @param route
     * @param initSlotToSearch
     * @param finalSlotToSearch
     * @param sizeOfReq
     * @return
     * @throws Exception
     */
    public static List<MSCLApeture> genApetures(Route route, int initSlotToSearch, int finalSlotToSearch) throws Exception{

        List<MSCLApeture> allApertures = new ArrayList<MSCLApeture>();

        int emptySlots = initSlotToSearch;

        if (emptySlots == -1){
            throw new Exception("Erro: emptySlots = -1");
        }

		INDEX_SLOT:for(int indexSlot = emptySlots; indexSlot <= finalSlotToSearch; indexSlot++){
            
            if (indexSlot == -1){
                throw new Exception("Erro: emptySlots = -1");
            }

            if (route.getSlotValue(indexSlot) > 0){
                continue INDEX_SLOT;
            }

            int countEmptySlots = 0;

			// Para cada slot necessário para alocar a requisição;
			EMPTY_SLOTS:for (emptySlots = indexSlot; emptySlots <= finalSlotToSearch; emptySlots++){

				if (route.getSlotValue(emptySlots) > 0){
                    break EMPTY_SLOTS;
                }

				countEmptySlots++;
			}

            allApertures.add(new MSCLApeture(indexSlot, countEmptySlots));

            indexSlot = emptySlots;
        }

		return allApertures;
    }
}
