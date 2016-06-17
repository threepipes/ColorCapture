import java.io.*;
import java.lang.String;
import java.lang.StringBuffer;
import java.security.*;
import java.util.*;

import javax.annotation.Generated;

class ColorCapture {
    boolean first;
    public ColorCapture() {
    	first = true;
    }
    int getColor(char c) {
        return (int)(c - 'A');
    }
    int[][] map;
    int[][] cmap;
    int D, DD;
    int pc, ec, nc;
    int maxColor;
    final int[] dy = {0, 1, 0, -1};
    final int[] dx = {-1, 0, 1, 0};
    XorShift r = new XorShift();
    int[] hashMap;
    Area area;
    int makeTurn(String[] board, int timeLeftMs) {
    	// have a color as a shifted bit
    	pc = 1<<board[0].charAt(0)-'A';
    	ec = 1<<board[D-1].charAt(D-1)-'A';
    	if(first){
    		first = false;
    		init(board);
        	area.next(0, pc);
    	}
    	area.next(1, ec);
    	
    	final int nextColor = getNextColor();
    	area.next(0, nextColor);
    	return nextColor;
    }
    
    int getNextColor(){
    	
    }
    
    int beamSearch(int turn, int wid, int decreseRate){
    	TreeSet<Area> qu = new TreeSet<>();
    	TreeSet<Area> nq = new TreeSet<>();
    	qu.add(area.getCopy());
    	for(int t=0; t<turn; t++){
    		while(!qu.isEmpty()){
    			Area a = qu.pollFirst();
    			
    		}
    		wid -= decreseRate;
    	}
    }
    
    void init(String[] board){
    	D = board.length;
    	DD = D*D;
    	char[][] tmp = new char[D][D];
    	map = new int[D][D];
    	cmap = new int[D][D];
    	for(int i=0; i<D; i++){
    		tmp[i] = board[i].toCharArray();
    		for(int j=0; j<D; j++){
    			cmap[i][j] = tmp[i][j]-'A';
    			map[i][j] = 1<<cmap[i][j];
    			maxColor = Math.max(maxColor, cmap[i][j]);
    		}
    	}
    	nc = 0;
    	while(nc==pc || nc==ec) nc++;
    	hashMap = new int[DD];
    	for(int i=0; i<DD; i++){
    		hashMap[i] = r.nextInt();
    	}
    	area = new Area();
    }
    
    class Area implements Comparable<Area>{
    	BitSet area, border, earea, eborder;
    	int pc, ec, size;
    	int hash;
    	public Area() {
    		area = new BitSet(DD);
    		earea = new BitSet(DD);
    		border = new BitSet(DD);
    		eborder = new BitSet(DD);
    		area.set(0); border.set(0);
    		earea.set(DD-1); eborder.set(DD-1);
    		size = 1;
    	}
    	public Area(BitSet a, BitSet b, BitSet ea, BitSet eb, int pc, int ec, int size){
    		area = a;
    		earea = ea;
    		border = b;
    		eborder = eb;
    		this.pc = pc;
    		this.ec = ec;
    		this.size = size;
    	}
    	Area getCopy(){
    		return new Area((BitSet)area.clone(), (BitSet)border.clone()
    				, (BitSet)earea.clone(), (BitSet)eborder.clone(), pc, ec, size);
    	}
    	
    	/**
    	 * get colors next to player's area
    	 * @param area	current player's area
    	 * @param pc	current player's color
    	 * @param ec	opponent's color
    	 */
    	int validColors(BitSet area, int pc, int ec){
    		int res = 0;
    		for(int i=border.nextSetBit(0); i!=-1; i=border.nextSetBit(i+1)){
    			final int y = i/D;
    			final int x = i%D;
    			for(int d=0; d<4; d++){
    				final int ny = y+dy[d];
    				final int nx = x+dx[d];
    				final int np = ny*D+nx;
    				if(out(ny, nx)
    						|| area.get(np)
    						|| earea.get(np))
    					continue;
    				res |= map[ny][nx];
    			}
    		}
    		return res & ~pc & ~ec;
    	}
    	/**
    	 * count validColors in num[]
    	 */
    	void validColors(BitSet area, int pc, int ec, int[] num){
    		Arrays.fill(num, 0);
    		for(int i=border.nextSetBit(0); i!=-1; i=border.nextSetBit(i+1)){
    			final int y = i/D;
    			final int x = i%D;
    			for(int d=0; d<4; d++){
    				final int ny = y+dy[d];
    				final int nx = x+dx[d];
    				final int np = ny*D+nx;
    				if(out(ny, nx)
    						|| area.get(np)
    						|| earea.get(np))
    					continue;
    				num[cmap[ny][nx]]++;
    			}
    		}
    	}
    	
    	void next(int player, int color){
    		if(player==0){
    			pc = color;
    			next(color, area, border, earea);
    		}else{
    			ec = color;
    			next(color, earea, eborder, area);
    		}
    	}
    	/**
    	 * Set bitsets to next state.
    	 * If enemy's turn, area is earea, border is eborder, earea is player's area
    	 * @param color		next color that current player wants to be
    	 * @param area		current player's area
    	 * @param border	current player's border
    	 * @param earea		opponent player's area
    	 * 
    	 */
    	private void next(int color, BitSet area, BitSet border, BitSet earea){
    		for(int i=border.nextSetBit(0); i!=-1; i=border.nextSetBit(i+1)){
    			final int y = i/D;
    			final int x = i%D;
    			for(int d=0; d<4; d++){
    				final int ny = y+dy[d];
    				final int nx = x+dx[d];
    				final int np = ny*D+nx;
    				if(out(ny, nx)
    						|| area.get(np)
    						|| earea.get(np)
    						|| map[ny][nx]!=color)
    					continue;
    				border.set(np);
    				area.set(np);
    				size++;
    			}
    		}
    		hash = 0;
    		for(int i=border.nextSetBit(0); i!=-1; i=border.nextSetBit(i+1)){
    			final int y = i/D;
    			final int x = i%D;
    			int adj = 0;
    			for(int d=0; d<4; d++){
    				final int ny = y+dy[d];
    				final int nx = x+dx[d];
    				final int np = ny*D+nx;
    				if(out(ny, nx) || earea.get(np) || area.get(np))
    					continue;
    				adj++;
    				boolean haveUncontrolled = false;
    				for(int e=0; e<4; e++){
    					final int nny = ny+dy[d];
    					final int nnx = ny+dx[d];
    					if(!out(ny, nx) && !area.get(nny*D+nnx)){
    						haveUncontrolled = true;
    						break;
    					}
    				}
    				if(!haveUncontrolled){
    					border.set(np);
    					area.set(np);
    					size++;
    				}
    			}
    			if(adj==0) border.clear(y*D+x);
    			else hash ^= hashMap[y*D+x];
    		}
    	}
    	@Override
    	public int compareTo(Area o) {
    		if(o.size != size) return o.size-size;
    		return o.hash-hash;
    	}
    	@Override
    	public int hashCode() {
    		return hash;
    	}
    }
    
    boolean out(int y, int x){
    	return y<0 || x<0 || y>=D || x>=D;
    }
}

class XorShift {
	int x = 123456789;
	int y = 362436069;
	int z = 521288629;
	int w = 88675123;
	int nextInt(int n) {
		final int t = x ^ (x << 11);
		x = y; y = z; z = w;
		w = (w ^ (w >>> 19)) ^ (t ^ (t >>> 8));
		final int r = w % n;
		return r < 0 ? r + n : r;
	}
	int nextInt() {
		final int t = x ^ (x << 11);
		x = y; y = z; z = w;
		return w = (w ^ (w >>> 19)) ^ (t ^ (t >>> 8));
	}
}
