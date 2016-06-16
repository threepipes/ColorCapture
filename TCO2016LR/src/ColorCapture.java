import java.io.*;
import java.lang.String;
import java.lang.StringBuffer;
import java.security.*;
import java.util.*;

class ColorCapture {
    SecureRandom r1;
    public ColorCapture() {
        try {
          r1 = SecureRandom.getInstance("SHA1PRNG");
          r1.setSeed(12345);
        }
        catch (NoSuchAlgorithmException e) { }
    }
    int getColor(char c) {
        return (int)(c - 'A');
    }
    int makeTurn(String[] board, int timeLeftMs) {
        int D = board.length;
        // get list of all colors present on the board adjacent to area controlled by player
        // bfs starting with corner cell already controlled by player
        int[] r = new int[D * D];
        int[] c = new int[D * D];
        boolean[][] vis = new boolean[D][D];
        int n = 0;

        int r0 = 0, c0 = 0;
        char playerColor = board[r0].charAt(c0);
        vis[r0][c0] = true;
        r[n] = r0;
        c[n] = c0;
        ++n;

        HashSet<Integer> colors = new HashSet<>();
        int maxC = 4;
        final int[] dr = {0, 1, 0, -1};
        final int[] dc = {-1, 0, 1, 0};
        int ind = 0;
        while (ind < n) {
            for (int d = 0; d < 4; ++d) {
                int newr = r[ind] + dr[d];
                int newc = c[ind] + dc[d];
                if (newr < 0 || newc < 0 || newr >= D || newc >= D)
                    continue;
                if (!vis[newr][newc]) {
                    if (board[newr].charAt(newc) == playerColor) {
                        vis[newr][newc] = true;
                        r[n] = newr;
                        c[n] = newc;
                        ++n;
                    } else {
                        // adjacent to one of player's cells but of different color - keep
                        int col = getColor(board[newr].charAt(newc));
                        colors.add(new Integer(col));
                        if (col > maxC)
                            maxC = col;
                    }
                }
            }
            ++ind;
        }

        // can't switch to colors currently used by players
        int col0 = getColor(board[0].charAt(0));
        int col1 = getColor(board[D - 1].charAt(D - 1));
        colors.remove(new Integer(col0));
        colors.remove(new Integer(col1));

        // if there are any other colors available, switch to one of them at random
        if (!colors.isEmpty()) {
            Integer[] cols = colors.toArray(new Integer[0]);
            return cols[r1.nextInt(cols.length)];
        }
        // if there are no good moves, do something
        int cret = r1.nextInt(maxC - 2);
        if (cret >= Math.min(col0, col1))
            ++cret;
        if (cret >= Math.max(col0, col1))
            ++cret;
        return cret;
    }
    // -------8<------- end of solution submitted to the website -------8<-------
    public static void main(String[] args) {
      try {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ColorCapture cc = new ColorCapture();
        while (true) {
            int N = Integer.parseInt(br.readLine());
            String[] board = new String[N];
            for (int i = 0; i < N; ++i) {
                board[i] = br.readLine();
            }
            int timeLeftMs = Integer.parseInt(br.readLine());
            int ret = cc.makeTurn(board, timeLeftMs);
            System.out.println(ret);
        }
      }
      catch (Exception e) {}
    }
}
