/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE"
 * As long as you retain this notice you can do whatever you want with this
 * stuff. If you meet an employee from Windward some day, and you think this
 * stuff is worth it, you can buy them a beer in return. Windward Studios
 * ----------------------------------------------------------------------------
 */

package net.windward.Acquire.AI;

import net.windward.Acquire.Units.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;


/**
 * The sample C# AI. Start with this project but write your own code as this is a very simplistic implementation of the AI.
 */
public class MyPlayerBrain {
	// bugbug - put your team name here.
	private static String NAME = "Fear The Dora";
                private int round = 0;
                private enum TileType {
                    SINGTON, FORMCHAIN, MERGE, NOTHING
                }
                

	// bugbug - put your school name here. Must be 11 letters or less (ie use MIT, not Massachussets Institute of Technology).
	public static String SCHOOL = "UMD";

	private static Logger log = Logger.getLogger(MyPlayerBrain.class);

	/**
	 * The name of the player.
	 */
	private String privateName;

	public final String getName() {
		return privateName;
	}

	private void setName(String value) {
		privateName = value;
	}

	private static final java.util.Random rand = new java.util.Random();

	public MyPlayerBrain(String name) {
		setName(!net.windward.Acquire.DotNetToJavaStringHelper.isNullOrEmpty(name) ? name : NAME);
	}

	/**
	 * The avatar of the player. Must be 32 x 32.
	 */
	public final byte[] getAvatar() {
		try {
			// open image
			InputStream stream = getClass().getResourceAsStream("/net/windward/Acquire/res/MyAvatar.png");

			byte[] avatar = new byte[stream.available()];
			stream.read(avatar, 0, avatar.length);
			return avatar;

		} catch (IOException e) {
			System.out.println("error reading image");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Called when the game starts, providing all info.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 */
	public void Setup(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
		// get your AI initialized here.
	}

	/**
	 * Asks if you want to play the CARD.DRAW_5_TILES or CARD.PLACE_4_TILES special power. This call will not be made
	 * if you have already played these cards.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 * @return CARD.NONE, CARD.PLACE_4_TILES, or CARD.DRAW_5_TILES.
	 */
	public int QuerySpecialPowerBeforeTurn(GameMap map, Player me, List<HotelChain> hotelChains,
	                                       List<Player> players) {
		// we randomly decide if we want to play a card.
		// We don't worry if we still have the card as the server will ignore trying to use a card twice.
		if (round == 3)
			return SpecialPowers.CARD_DRAW_5_TILES;
		if (round == 4)
			return SpecialPowers.CARD_PLACE_4_TILES;
		return SpecialPowers.CARD_NONE;
	}

	/**
	 * Return what tile to play when using the CARD.PLACE_4_TILES. This will be called for the first 3 tiles and is for
	 * placement only. Any merges due to this will be resolved before the next card is played. For the 4th tile,
	 * QueryTileAndPurchase will be called.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 * @return The tile(s) to play and the stock to purchase (and trade if CARD.TRADE_2_STOCK is played).
	 */
	public PlayerPlayTile QueryTileOnly(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
TileType type;
                    HashMap<PlayerTile, Integer> tileMap = new HashMap<PlayerTile, Integer>();
                    round++;
                    PlayerPlayTile turn = new PlayerTurn();
                    for (PlayerTile tile : me.getTiles()) {
                        int x = tile.getX();
                        int y = tile.getY();
                        type = checkType(tile, map, me, hotelChains, players);
                        switch (type) {
                            case SINGTON: tileMap.put(tile, SingtonValue(tile, map, me, hotelChains, players));
                                                break;
                            case MERGE: tileMap.put(tile, MergeValue(tile, map, me, hotelChains, players));
                                                break;
                            case FORMCHAIN: tileMap.put(tile, FormChainValue(tile, map, me, hotelChains, players));
                                                break;
                            default: tileMap.put(tile, new Integer(0));
                                        break;
                        }
                    }
                    
                    int max = 0;
                    PlayerTile selected = me.getTiles().get(rand.nextInt(me.getTiles().size()));
                    
                    Set<PlayerTile> tileSet = tileMap.keySet();
                    
                    for (PlayerTile t : tileSet) {
                        if (tileMap.get(t).intValue() > max) {
                            max = tileMap.get(t).intValue();
                            selected = t;
                        }
                    }
                    
                   turn.tile = selected;
                   type = checkType(selected, map, me, hotelChains, players);
                   /* Still need to set createdHotel and MergeSurvivor */
                   
                   if (type != TileType.FORMCHAIN) {
                   HashMap <HotelChain, Double> score = new HashMap<HotelChain, Double>();
                   List<HotelChain> avaliable = new ArrayList<HotelChain>();
                   
                   for (HotelChain hotel : hotelChains) {
                       if (hotel.getNumAvailableShares() > 0 && !hotel.isSafe() && hotel.isActive())
                           avaliable.add(hotel);
                           
                   }
                   
                   for (HotelChain hotel : avaliable) {
                       Integer s = DiffToTop(me, hotel, players);
                       int m = hotel.getStockPrice()/hotel.getNumTiles();
                       Double ss = new Double(((double)s.intValue()) / (double)(s));
                       score.put(hotel, ss);
                   }
                   
                   max = 0;
                   
                   Set<HotelChain> scoreSet = score.keySet();
                   HotelChain hhh = null;
                   
                   for (HotelChain h : scoreSet) {
                       if (score.get(h).intValue() > max) {
                           max = score.get(h).intValue();
                           hhh = h;
                       }
                   }
                   int iaopdiwpoai = 0;
                   int total = 3;
                   if (hhh != null) {
                       if (total < hhh.getNumAvailableShares())
                           iaopdiwpoai = 0;
                       else {
                           total -= hhh.getFirstMajorityBonus();
                           score.remove(hhh);
                           
                           max = 0;
                   
                   scoreSet = score.keySet();
                   hhh = null;
                   
                   for (HotelChain h : scoreSet) {
                       if (score.get(h).intValue() > max) {
                           max = score.get(h).intValue();
                           hhh = h;
                       }
                   }
                   
                   if (hhh != null) {
                       if (total < hhh.getNumAvailableShares())
                           iaopdiwpoai = 0;//turn.getBuy().add(new HotelStock(hhh, total));
                       else {
                           iaopdiwpoai = 0;//turn.getBuy().add(new HotelStock(hhh, total - hhh.getFirstMajorityBonus()));
                           total -= hhh.getFirstMajorityBonus();
                           score.remove(hhh);
                           
                           max = 0;
                   
                   scoreSet = score.keySet();
                   hhh = null;
                   
                   for (HotelChain h : scoreSet) {
                       if (score.get(h).intValue() > max) {
                           max = score.get(h).intValue();
                           hhh = h;
                       }
                   }
                   
                   //if (hhh != null) turn.getBuy().add(new HotelStock(hhh, 1));
                       }
                   }
                       }
                   }
        
                   
                   } else {
                       HashSet<HotelChain> nactiveh = new HashSet<HotelChain>();
                       for (HotelChain hs : hotelChains) {
                           if (!hs.isActive())
                               nactiveh.add(hs);
                       }
                       max = 0;
                       for (HotelChain hs : nactiveh)
                           if (hs.getStartPrice() > max)
                               max = hs.getStartPrice();
                       
                       HotelChain sl = null;
                       for (HotelChain hs : nactiveh)
                           if (hs.getStartPrice() == max) {
                               turn.createdHotel = hs;
                               //turn.getBuy().add(new HotelStock(hs, 3));
                           }
                   }
                   
                   if (type == TileType.MERGE) {
                       HotelChain h1 = null;
                       HotelChain h2 = null;
                       
                       int x = selected.getX();
                       int y = selected.getY();
                       
                       int[] dx = {0, -1, 1, 0};
                       int[] dy = {-1, 0, 0, 1};
                       
                       int i;
                       
                       for (i = 0; i < 4; i++) {
                           int xx = dx[0] + x;
                           int yy = dy[0] + y;
                           
                           if (xx > 0 && yy < 0 && xx < map.getWidth() && yy < map.getHeight()) {
                            MapTile current = map.getTiles(xx, yy);
                            if (current.getType() == MapTile.TYPE_HOTEL) {
                                h1 = current.getHotel();
                                break;
                            }
                           }
                       }
                       int j = i + 1;
                       for (i = j; i < 4; i++) {
                           int xx = dx[0] + x;
                           int yy = dy[0] + y;
                           
                           if (xx > 0 && yy < 0 && xx < map.getWidth() && yy < map.getHeight()) {
                            MapTile current = map.getTiles(xx, yy);
                            if (current.getType() == MapTile.TYPE_HOTEL) {
                                h2 = current.getHotel();
                                break;
                            }
                           }
                       }
                       
                       List<StockOwner> h1o = h1.getFirstMajorityOwners();
                       List<StockOwner> h2o = h2.getFirstMajorityOwners();
                       
                       List<Player> p1 = new ArrayList<Player>();
                       for (StockOwner sss : h1o) {
                           p1.add(sss.getOwner());
                       }
                       
                       if (p1.contains(me)) {
                           turn.mergeSurvivor = h1;
                       } else {
                           List<Player> p2 = new ArrayList<Player>();
                       for (StockOwner sss : h2o) {
                           p2.add(sss.getOwner());
                       }
                       
                        if (p2.contains(me)) {
                            turn.mergeSurvivor = h2;
                        } else {
                           h1o = h1.getSecondMajorityOwners();
                       h2o = h2.getSecondMajorityOwners();
                            p1 = new ArrayList<Player>();
                       for (StockOwner sss : h1o) {
                           p1.add(sss.getOwner());
                       }
                       if (p1.contains(me)) {
                           turn.mergeSurvivor = h1;
                       } else {
                           p2 = new ArrayList<Player>();
                       for (StockOwner sss : h2o) {
                           p2.add(sss.getOwner());
                       }
                       if (p2.contains(me)) {
                           turn.mergeSurvivor = h2;
                       } else {
                           turn.mergeSurvivor = h1;
                       }
                       }
                        }
                       } 
                       
                       
                   }
                   
                   
		/*PlayerPlayTile playTile = new PlayerPlayTile();
		// we select a tile at random from our set
		playTile.tile = me.getTiles().size() == 0 ? null : me.getTiles().get(rand.nextInt(me.getTiles().size()));
		// we grab a random available hotel as the created hotel in case this tile creates a hotel
		for (HotelChain hotel : hotelChains)
			if (! hotel.isActive()) {
				playTile.createdHotel = hotel;
				break;
			}
		// We grab an existing hotel at random in case this tile merges multiple chains.
		// note - the surviror may not be one of the hotels merged (this is a very stupid AI)!
		for (HotelChain hotel : hotelChains)
			if (hotel.isActive()) {
				playTile.mergeSurvivor = hotel;
				break;
			}
		return playTile;*/
                return turn;
	}
        
                private boolean Sington(PlayerTile tile, GameMap map) {
                    int x = tile.getX();
                    int y = tile.getY();
                    
                    int[] dx = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
                    int[] dy = {-1, -1, -1, 0, 0, 0, 1, 1, 1};
                    
                    for (int i = 0; i < 9; i++) {
                        int xx = dx[i] + x;
                        int yy = dy[i] + y;
                        if (xx > 0 && yy < 0 && xx < map.getWidth() && yy < map.getHeight()) {
                            MapTile current = map.getTiles(tile.getX(), tile.getY());
                            if (current.getType() != MapTile.TYPE_UNDEVELOPED)
                                return false;
                        }
                    }
                    
                    return true;
                }
                
                private boolean Merge(PlayerTile tile, GameMap map) {
                    int x = tile.getX();
                    int y = tile.getY();
                    HashSet<String> hotels = new HashSet<String>();
                    
                    int[] dx = {0, -1, 1, 0};
                    int[] dy = {-1, 0, 0, 1};
                    
                    for (int i = 0; i < 4; i++) {
                        int xx = dx[i] + x;
                        int yy = dy[i] + y;
                        if (xx > 0 && yy < 0 && xx < map.getWidth() && yy < map.getHeight()) {
                            MapTile current = map.getTiles(xx, yy);
                            if (current.getType() != MapTile.TYPE_UNPLAYABLE_MERGE_SAFE) hotels.add(current.getHotel().getName());
                        }
                    }
                    
                    return hotels.size() >= 2;
                    
                }
                
                private boolean FormChain(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
                    boolean chain = false;
for(PlayerTile tile : me.getTiles()) {
   if( tile.getX() < map.getWidth() && tile.getY() < map.getHeight()) {
      if(map.getTiles(tile.getX()-1, tile.getY()).getType() == MapTile.TYPE_SINGLE ||
            map.getTiles(tile.getX()+1, tile.getY()).getType() == MapTile.TYPE_SINGLE  ||
            map.getTiles(tile.getX(), tile.getY()-1).getType() == MapTile.TYPE_SINGLE  ||
            map.getTiles(tile.getX(), tile.getY()+1).getType() == MapTile.TYPE_SINGLE) {
         chain = true;
      }
   }
   

                }
return chain;
                }
                
                private TileType checkType(PlayerTile tile, GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
                    if (Sington(tile, map))
                        return TileType.SINGTON;
                    if (Merge(tile, map))
                        return TileType.MERGE;
                    if (FormChain(map, me, hotelChains, players))
                        return TileType.FORMCHAIN;
                    
                    return TileType.NOTHING;
                }
                
                private Integer SingtonValue(PlayerTile tile, GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
                    
                    return new Integer(10);
                }
                
                private Integer MergeValue(PlayerTile tile, GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
                    
                    return new Integer(50);
                }
                
                private Integer FormChainValue(PlayerTile tile, GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
                    
                    return new Integer(100);
                }
                
                private Integer DiffToTop(Player me, HotelChain hotel, List<Player> players) {
                    List<HotelStock> hsls = me.getStock();
                    HotelStock hs = null;
                    int hold = 0;
                    
                    for (HotelStock h : hsls) {
                        if (h.getChain().getName().equals(hotel.getName())) {
                            hs = h;
                            break;
                        }
                    }
                    
                    if (hs != null)
                        hold = hs.getNumShares();
                    
                    int maxOtherHold = 0;
                    
                    for (Player p : players) {
                        if (!p.getName().equals(me.getName())) {
                            List<HotelStock> phsls = p.getStock();
                            HotelStock phs = null;
                            int phold = 0;
                            
                            for (HotelStock h : phsls) {
                                if (h.getChain().getName().equals(hotel.getName())) {
                                    phs = h;
                                    break;
                                }
                            }
                            
                            if (phs != null)
                                phold = phs.getNumShares();
                            
                            if (phold > maxOtherHold)
                                maxOtherHold = phold;
                        }         
                    }
                    
                    int diff = maxOtherHold - hold;
                    
                    
                    
                    return new Integer(diff);
                }
                
       
                
	/**
	 * Return what tile(s) to play and what stock(s) to purchase. At this point merges have not yet been processed.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 * @return The tile(s) to play and the stock to purchase (and trade if CARD.TRADE_2_STOCK is played).
	 */
	public PlayerTurn QueryTileAndPurchase(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players) {
round = 0;
                    TileType type;
                    HashMap<PlayerTile, Integer> tileMap = new HashMap<PlayerTile, Integer>();
                    PlayerTurn turn = new PlayerTurn();
                    for (PlayerTile tile : me.getTiles()) {
                        int x = tile.getX();
                        int y = tile.getY();
                        type = checkType(tile, map, me, hotelChains, players);
                        switch (type) {
                            case SINGTON: tileMap.put(tile, SingtonValue(tile, map, me, hotelChains, players));
                                                break;
                            case MERGE: tileMap.put(tile, MergeValue(tile, map, me, hotelChains, players));
                                                break;
                            case FORMCHAIN: tileMap.put(tile, FormChainValue(tile, map, me, hotelChains, players));
                                                break;
                            default: tileMap.put(tile, new Integer(0));
                                        break;
                        }
                    }
                    
                    int max = 0;
                    PlayerTile selected = me.getTiles().get(rand.nextInt(me.getTiles().size()));
                    
                    Set<PlayerTile> tileSet = tileMap.keySet();
                    
                    for (PlayerTile t : tileSet) {
                        if (tileMap.get(t).intValue() > max) {
                            max = tileMap.get(t).intValue();
                            selected = t;
                        }
                    }
                    
                   turn.tile = selected;
                   type = checkType(selected, map, me, hotelChains, players);
                   /* Still need to set createdHotel and MergeSurvivor */
                   
                   if (type != TileType.FORMCHAIN) {
                   HashMap <HotelChain, Double> score = new HashMap<HotelChain, Double>();
                   List<HotelChain> avaliable = new ArrayList<HotelChain>();
                   
                   for (HotelChain hotel : hotelChains) {
                       if (hotel.getNumAvailableShares() > 0 && !hotel.isSafe() && hotel.isActive())
                           avaliable.add(hotel);
                           
                   }
                   
                   for (HotelChain hotel : avaliable) {
                       Integer s = DiffToTop(me, hotel, players);
                       int m = hotel.getStockPrice()/hotel.getNumTiles();
                       Double ss = new Double(((double)s.intValue()) / (double)(s));
                       score.put(hotel, ss);
                   }
                   
                   max = 0;
                   
                   Set<HotelChain> scoreSet = score.keySet();
                   HotelChain hhh = null;
                   
                   for (HotelChain h : scoreSet) {
                       if (score.get(h).intValue() > max) {
                           max = score.get(h).intValue();
                           hhh = h;
                       }
                   }
                   
                   int total = 3;
                   if (hhh != null) {
                       if (total < hhh.getNumAvailableShares())
                           turn.getBuy().add(new HotelStock(hhh, 3));
                       else {
                           turn.getBuy().add(new HotelStock(hhh, total - hhh.getFirstMajorityBonus()));
                           total -= hhh.getFirstMajorityBonus();
                           score.remove(hhh);
                           
                           max = 0;
                   
                   scoreSet = score.keySet();
                   hhh = null;
                   
                   for (HotelChain h : scoreSet) {
                       if (score.get(h).intValue() > max) {
                           max = score.get(h).intValue();
                           hhh = h;
                       }
                   }
                   
                   if (hhh != null) {
                       if (total < hhh.getNumAvailableShares())
                           turn.getBuy().add(new HotelStock(hhh, total));
                       else {
                           turn.getBuy().add(new HotelStock(hhh, total - hhh.getFirstMajorityBonus()));
                           total -= hhh.getFirstMajorityBonus();
                           score.remove(hhh);
                           
                           max = 0;
                   
                   scoreSet = score.keySet();
                   hhh = null;
                   
                   for (HotelChain h : scoreSet) {
                       if (score.get(h).intValue() > max) {
                           max = score.get(h).intValue();
                           hhh = h;
                       }
                   }
                   
                   if (hhh != null) turn.getBuy().add(new HotelStock(hhh, 1));
                       }
                   }
                       }
                   }
        
                   
                   } else {
                       HashSet<HotelChain> nactiveh = new HashSet<HotelChain>();
                       for (HotelChain hs : hotelChains) {
                           if (!hs.isActive())
                               nactiveh.add(hs);
                       }
                       max = 0;
                       for (HotelChain hs : nactiveh)
                           if (hs.getStartPrice() > max)
                               max = hs.getStartPrice();
                       
                       HotelChain sl = null;
                       for (HotelChain hs : nactiveh)
                           if (hs.getStartPrice() == max) {
                               turn.createdHotel = hs;
                               turn.getBuy().add(new HotelStock(hs, 3));
                           }
                   }
                   
                   if (type == TileType.MERGE) {
                       HotelChain h1 = null;
                       HotelChain h2 = null;
                       
                       int x = selected.getX();
                       int y = selected.getY();
                       
                       int[] dx = {0, -1, 1, 0};
                       int[] dy = {-1, 0, 0, 1};
                       
                       int i;
                       
                       for (i = 0; i < 4; i++) {
                           int xx = dx[0] + x;
                           int yy = dy[0] + y;
                           
                           if (xx > 0 && yy < 0 && xx < map.getWidth() && yy < map.getHeight()) {
                            MapTile current = map.getTiles(xx, yy);
                            if (current.getType() == MapTile.TYPE_HOTEL) {
                                h1 = current.getHotel();
                                break;
                            }
                           }
                       }
                       int j = i + 1;
                       for (i = j; i < 4; i++) {
                           int xx = dx[0] + x;
                           int yy = dy[0] + y;
                           
                           if (xx > 0 && yy < 0 && xx < map.getWidth() && yy < map.getHeight()) {
                            MapTile current = map.getTiles(xx, yy);
                            if (current.getType() == MapTile.TYPE_HOTEL) {
                                h2 = current.getHotel();
                                break;
                            }
                           }
                       }
                       
                       List<StockOwner> h1o = h1.getFirstMajorityOwners();
                       List<StockOwner> h2o = h2.getFirstMajorityOwners();
                       
                       List<Player> p1 = new ArrayList<Player>();
                       for (StockOwner sss : h1o) {
                           p1.add(sss.getOwner());
                       }
                       
                       if (p1.contains(me)) {
                           turn.mergeSurvivor = h1;
                       } else {
                           List<Player> p2 = new ArrayList<Player>();
                       for (StockOwner sss : h2o) {
                           p2.add(sss.getOwner());
                       }
                       
                        if (p2.contains(me)) {
                            turn.mergeSurvivor = h2;
                        } else {
                           h1o = h1.getSecondMajorityOwners();
                       h2o = h2.getSecondMajorityOwners();
                            p1 = new ArrayList<Player>();
                       for (StockOwner sss : h1o) {
                           p1.add(sss.getOwner());
                       }
                       if (p1.contains(me)) {
                           turn.mergeSurvivor = h1;
                       } else {
                           p2 = new ArrayList<Player>();
                       for (StockOwner sss : h2o) {
                           p2.add(sss.getOwner());
                       }
                       if (p2.contains(me)) {
                           turn.mergeSurvivor = h2;
                       } else {
                           turn.mergeSurvivor = h1;
                       }
                       }
                        }
                       } 
                       
                       
                   }
                       
                   
                   
                   
                    
                
		// randomly occasionally play one of the cards
		// We don't worry if we still have the card as the server will ignore trying to use a card twice.
		switch (round) {
			case 6:
				turn.setCard(SpecialPowers.CARD_BUY_5_STOCK);
				turn.getBuy().add(new HotelStock(hotelChains.get(rand.nextInt(hotelChains.size())), 3));
				return turn;
			case 7:
				turn.setCard(SpecialPowers.CARD_FREE_3_STOCK);
				return turn;
			default:
				
				return turn;
		}
                //return turn;
	}

	/**
	 * Ask the AI what they want to do with their merged stock. If a merge is for 3+ chains, this will get called once
	 * per removed chain.
	 * @param map The game map.
	 * @param me The player being setup.
	 * @param hotelChains All hotel chains.
	 * @param players All the players.
	 * @param survivor The hotel that survived the merge.
	 * @param defunct The hotel that is now defunct.
	 * @return What you want to do with the stock.
	 */
	public PlayerMerge QueryMergeStock(GameMap map, Player me, List<HotelChain> hotelChains, List<Player> players,
	                                   HotelChain survivor, HotelChain defunct) {
		HotelStock myStock = null;
		for (HotelStock stock : me.getStock())
			if (stock.getChain() == defunct) {
				myStock = stock;
				break;
			}
		// we sell, keep, & trade 1/3 of our shares in the defunct hotel
		return new PlayerMerge(myStock.getNumShares(), 0, 0);
	}
}