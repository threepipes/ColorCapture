import java.util.Arrays;
import java.util.BitSet;
import java.util.Map.Entry;
import java.util.TreeMap;

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
	int[] yq, xq;
	int right, left;
	BitSet used;
	int D, DD, DD2;
	int pc, ec, pci, eci, nci;
	int maxColor;
	final int[] dy = {0, 1, 0, -1};
	final int[] dx = {-1, 0, 1, 0};
	XorShift r = new XorShift();
	int[] hashMap;
	Area area;
	int[] num;
	int makeTurn(String[] board, int timeLeftMs) {
		if(first){
			first = false;
			init(board);
			area.next(0, pc);
		}
		if(!check(board)){
			System.err.println("Wrong");
		}
		// have a color as a shifted bit
		pci = board[0].charAt(0)-'A';
		pc = 1<<pci;
		eci = board[D-1].charAt(D-1)-'A';
		ec = 1<<eci;
		nci = 0;
		while(nci==pci || nci==eci) nci++;
		area.next(1, ec);

		final int nextColor = getNextColor();
		area.next(0, 1<<nextColor);
		return nextColor;
	}
	
	boolean check(String[] board){
		char[][] mp = new char[D][D];
		for(int i=0; i<D; i++){
			mp[i] = board[i].toCharArray();
		}
		left = 0; right = 1;
		yq[0] = 0;
		xq[0] = 0;
		used.clear();
		used.set(0);
		while(left<right){
			final int y = yq[left];
			final int x = xq[left];
			left++;
			for(int d=0; d<4; d++){
				final int ny = y+dy[d];
				final int nx = x+dx[d];
				if(out(ny, nx) || used.get(ny*D+nx)) continue;
				used.set(ny*D+nx);
				yq[right] = ny;
				xq[right] = nx;
				right++;
			}
		}
		used.flip(0, DD);
		return !used.intersects(area.area);
	}

	int getNextColor(){
		weightArea = 0.5;
		if(D<30){
			return beamSearch(5, 80, 3, true);
		}
		else if(D<50){
			if((double)area.area.cardinality()/DD<0.2)
				return greedyFarest();
			return beamSearch(3, 40, 10, true);
		}
		else{
//			weightArea = area.area.cardinality()/DD;
			return greedyFarest();
		}
	}
	
	int greedyFarest(){
		return area.farestColor();
	}

	int beamSearch(int turn, int wid, int decreseRate, boolean moveEnemy){
		TreeMap<Area, Integer> qu = new TreeMap<>();
		TreeMap<Area, Integer> nq = new TreeMap<>();
		qu.put(area.getCopy(), -1);
		for(int t=0; t<turn; t++){
			while(!qu.isEmpty()){
				Entry<Area, Integer> e = qu.pollFirstEntry();
				setNextStates(nq, wid, e.getKey(), e.getValue(), moveEnemy);
			}
			wid -= decreseRate;
			TreeMap<Area, Integer> tmp = qu;
			qu = nq; nq = tmp;
		}
		if(qu.isEmpty()){
			return nci;
		}
		Entry<Area, Integer> best = qu.pollFirstEntry();
		return best.getValue();
	}
	
	/*
	 * @param firstColor	not bit value, it's idx of color
	 */
	int max, maxIdx;
	void setNextStates(TreeMap<Area, Integer> qu, int wid, Area cur, int firstColor, boolean moveEnemy){
		int vc = cur.validColors(0);
		// search all. so if time complexity is large, select at random.
		for(int i=0; i<=maxColor; i++){
			if((vc&1<<i)==0) continue;
			Area newArea = cur.getCopy();
			newArea.next(0, 1<<i);
			if(moveEnemy){
				newArea.validColors(1, num);
				max = 0; maxIdx = 0;
				for(int j=0; j<=maxColor; j++){
					if(num[j]>max){
						max = num[j];
						maxIdx = j;
					}
				}
				newArea.next(1, 1<<maxIdx);
			}
			qu.put(newArea, firstColor==-1?i:firstColor);
			if(qu.size()>wid) qu.pollLastEntry();
		}
	}

	void init(String[] board){
		D = board.length;
		DD = D*D;
		DD2 = sq(D*2);
		yq = new int[DD];
		xq = new int[DD];
		used = new BitSet(DD);
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
		hashMap = new int[DD];
		for(int i=0; i<DD; i++){
			hashMap[i] = r.nextInt();
		}
		num = new int[maxColor+1];
		area = new Area();
	}
	static double weightArea = 0;
	class Area implements Comparable<Area>{
		BitSet area, border, earea, eborder;
		int pc, ec;
		double score;
		int hash;
		public Area() {
			area = new BitSet(DD);
			earea = new BitSet(DD);
			border = new BitSet(DD);
			eborder = new BitSet(DD);
			area.set(0); border.set(0);
			earea.set(DD-1); eborder.set(DD-1);
			score = 0;
		}
		public Area(BitSet a, BitSet b, BitSet ea, BitSet eb, int pc, int ec){
			area = a;
			earea = ea;
			border = b;
			eborder = eb;
			this.pc = pc;
			this.ec = ec;
		}
		Area getCopy(){
			return new Area((BitSet)area.clone(), (BitSet)border.clone()
					, (BitSet)earea.clone(), (BitSet)eborder.clone(), pc, ec);
		}

		int validColors(int player){
			if(player==0) return validColors(area, earea, pc, ec);
			else return validColors(earea, area, ec, pc);
		}
		/**
		 * get colors next to player's area
		 * @param area	current player's area
		 * @param pc	current player's color
		 * @param ec	opponent's color
		 */
		private int validColors(BitSet area, BitSet earea, int pc, int ec){
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
		
		int farestColor(){
			long dist = 0;
			int color = nci;
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
							|| cmap[ny][nx]==pci
							|| cmap[ny][nx]==eci)
						continue;
					long nd = ny*ny+nx*nx+DD-sq(nx-ny);
					if(nd>dist){
						dist = nd;
						color = cmap[ny][nx];
					}
				}
			}
			return color;
		}

		void validColors(int player, int[] num){
			if(player==0) validColors(area, earea, pc, ec, num);
			else validColors(earea, area, ec, pc, num);
		}
		/**
		 * count validColors in num[]
		 */
		private void validColors(BitSet area, BitSet earea, int pc, int ec, int[] num){
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
				culcScore();
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
				}
			}
			bfsForInnerArea(border, area, earea);
		}
		void bfsForInnerArea(BitSet border, BitSet area, BitSet earea){
			hash = 0;
			for(int i=border.nextSetBit(0); i!=-1; i=border.nextSetBit(i+1)){
				final int y = i/D;
				final int x = i%D;
				int adj = 0;
				out:for(int d=0; d<4; d++){
					final int ny = y+dy[d];
					final int nx = x+dx[d];
					final int np = ny*D+nx;
					if(out(ny, nx) || earea.get(np) || area.get(np))
						continue;
					adj++;
					used.clear();
					left = 0;
					right = 1;
					yq[0] = ny;
					xq[0] = nx;
					used.set(np);
					while(left<right){
						final int py = yq[left];
						final int px = xq[left];
						left++;
						for(int e=0; e<4; e++){
							final int nny = py+dy[e];
							final int nnx = px+dx[e];
							final int nnp = nny*D+nnx;
							if(out(nny, nnx) || used.get(nnp) || area.get(nnp)) continue;
							if(earea.get(nnp)) break out;
							used.set(nnp);
							yq[right] = nny;
							xq[right] = nnx;
							right++;
						}
					}
					area.or(used);
				}
				if(adj==0) border.clear(y*D+x);
				else hash ^= hashMap[y*D+x];
			}
		}
		void culcScore(){
			for(int i=border.nextSetBit(0); i!=-1; i=border.nextSetBit(i+1)){
				final int y = i/D;
				final int x = i%D;
				score += sq(y+x)/DD2;
			}
			score /= (border.cardinality())*(1-weightArea);
			score += (double)area.cardinality()/DD*weightArea;
		}
		@Override
		public int compareTo(Area o) {
			if(o.score != score) return Double.compare(o.score, score);
			return o.hash-hash;
		}
		@Override
		public int hashCode() {
			return hash;
		}
	}
	
	int sq(int a){
		return a*a;
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
