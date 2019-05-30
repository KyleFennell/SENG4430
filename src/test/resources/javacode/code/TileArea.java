package com.dcaiti.traceloader;

import java.util.ArrayList;
import java.util.List;

public class TileArea {
    
    public enum Area {GERMANY, STUTTGART_CITY, STUTTGART_AREA, STUTTGART_HIGHWAYS, INT1, BOEBLINGEN, STAT_TEST_CASE}

    
    public static List<Tile> getTiles(int zoom, Area area) {
        
        List<Tile> myTiles = new ArrayList<>();
        
        int[] minMax = determineTileRange(zoom, area);
        int minX = minMax[0];
        int maxX = minMax[1];
        int minY = minMax[2];
        int maxY = minMax[3];

        
        for(int x = minX; x <= maxX; x++){
            for(int y = minY; y <= maxY; y++){
                Tile tile = new Tile(zoom, x, y);
                myTiles.add(tile);
            }
        }
        System.out.println("No of tiles: "+myTiles.size());

        return myTiles;
        
    }
    
    
    public static String[] getTileIdsAsString(List<Tile> tiles){
        
        String[] tileIds = new String[tiles.size()];
        int i = 0;
        for (Tile t : tiles) {
            tileIds[i] = t.tileId;
            i++;
        }
        return tileIds;
    }


    /**
     * determine the min and max values for different zoom levels and predefined areas
     */
    public static int[] determineTileRange(int zoom, Area area){
    
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
    
        switch(zoom){
        case 12:
            switch(area) {
            case GERMANY:		  
                //jeweils äußerste Tiles in Deutschland
                minX = 2115;
                maxX = 2218;
                minY = 1298;
                maxY = 1435;
                break;
            case STUTTGART_AREA:
                //test values area Stuttgart
                minX = 2152;
                maxX = 2155;
                minY = 1410;
                maxY = 1411;
                break;
            case STUTTGART_CITY:
                minX = 2152;
                maxX = 2152;
                minY = 1410;
                maxY = 1410;
                break;
            default:
                System.err.println("Area "+area +" for Zoom "+zoom +" is not yet implemented");
            } break;
        case 16:
            switch(area) {
            case GERMANY:                 
                //real values for all of germany
                minX = 33836;
                maxX = 35504;
                minY = 20766;
                maxY = 22978;
                break;
            case STUTTGART_CITY:
                //test values area Stuttgart
                minX = 34432;
                maxX = 34447;
                minY = 22560;
                maxY = 22575;
                break;
            case STUTTGART_HIGHWAYS:
                //sample values stuttgart autobahn
                minX = 34416;
                maxX = 34418;
                minY = 22582;
                maxY = 22584;                                
                break;
            case INT1:
                minX = 34450;
                maxX = 34452;
                minY = 22550;
                maxY = 22552;                           
                break;
            case BOEBLINGEN:
                minX = 34401;
                maxX = 34408;
                minY = 22587;
                maxY = 22596;                           
                break;               
            default:
                System.err.println("Area "+area +" for Zoom "+zoom +" is not yet implemented");
            } break;
        case 17:
            switch(area) {
            case GERMANY:                 
                //real values for all of germany
                minX = 67672;
                maxX = 71010;
                minY = 41531;
                maxY = 45958;
                break;
            case STUTTGART_AREA:
                minX = 68864;
                maxX = 68995;
                minY = 45120;
                maxY = 45151;
                break;
            case STUTTGART_CITY:
                minX = 68864;
                maxX = 68895;
                minY = 45120;
                maxY = 45151;
                break;
            case BOEBLINGEN:  //completeDataDelivery7
                minX = 68796;
                maxX = 68831;
                minY = 45164;
                maxY = 45195;
                break;
            case STAT_TEST_CASE:
//              only for testing
//                minX = 68877;
//                maxX = 68878;
//                minY = 45133;
//                maxY = 45134;
              minX = 68804;
              minY = 45184;
              maxX = 68805;
              maxY = 45185;

                break;
            default:
                System.err.println("Area "+area +" for Zoom "+zoom +" is not yet implemented");
            } break;
        default:
            System.err.println("Zoom is not yet implemented");
        }
    
        //minX, maxX, minY, maxY
        int [] minMax = new int[4];
        minMax[0] = minX;
        minMax[1] = maxX;
        minMax[2] = minY;
        minMax[3] = maxY;
    
        return minMax;
    
    }


}
