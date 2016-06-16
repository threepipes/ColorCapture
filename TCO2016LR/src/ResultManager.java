import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ResultManager {
	private TreeMap<Integer, Points> result;
	private int better, worse;
	private double total;
	private int cases;
	private String filename;
	int min, max;
	public ResultManager(String filename, int testCaseMin, int testCaseMax){
		this.filename = filename;
		result = new TreeMap<Integer, Points>();
		better = 0;
		worse = 0;
		total = 0;
		cases = 0;
		min = testCaseMin;
		max = testCaseMax;
	}
	
	// format
	// line1:  number of result
	// line2~: seed point np N time
	public void read(){
		try {
			ContestScanner in = new ContestScanner(filename);
			int n = in.nextInt();
			for(int i=0; i<n; i++){
				int seed = in.nextInt();
				double point = in.nextDouble();
				result.put(seed, new Points(seed, point));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// TreeMapの内容をfileに書き出して閉じる
	public void write(){
		try {
			ContestWriter out = new ContestWriter(filename);
			out.println(""+result.size());
			while(!result.isEmpty()){
				out.println(result.pollFirstEntry().getValue().toString());
			}
			System.err.println("Written.");
			System.err.println("Better: "+better);
			System.err.println("Worse : "+worse);
			System.err.println("Total : "+(total/cases));
			writeTotal(total/cases);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	final static String totalFile = "%d-%d_total.txt";
	public void writeTotal(double totalScore) throws IOException{
		String filename = String.format(totalFile, min ,max);
		TreeMap<Double, String> map = new TreeMap<>();
		try{
			ContestScanner in = new ContestScanner(filename);
			int n = in.nextInt();
			for(int i=0; i<n; i++){
				double sc = in.nextDouble();
				String time = in.nextToken();
				map.put(sc, time);
			}
			in.close();
		}catch(FileNotFoundException e){
			System.err.println("Create "+filename);
		}
		//現在日時を取得する
		Calendar c = Calendar.getInstance();
		//フォーマットパターンを指定して表示する
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/hh:mm:ss");
		map.put(totalScore, sdf.format(c.getTime()));
		ContestWriter out = new ContestWriter(filename);
		out.println(""+map.size());
		for(Entry<Double, String> e: map.entrySet()){
			out.println(e.getKey()+" "+e.getValue());
		}
		out.close();
	}
	
	// filenameにoutputを出力して閉じる
	public void write(String filename, String output){
		try {
			ContestWriter out = new ContestWriter(filename);
			out.println(output);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setResult(int seed, double point){
		cases++;
		total += point;
		if(!result.containsKey(seed)){
			result.put(seed, new Points(seed, point));
			return;
		}
		Points old = result.get(seed);
		Points now = new Points(seed, point);
		System.out.flush();
		System.err.println("old score: "+old.toString());
		System.err.println("new score: "+now.toString());
		System.err.println("( diff: "+(now.point-old.point)+" )");
		if(old.point < now.point){
			System.err.println("BEST SCORE!");
			result.put(seed, now);
			better++;
//			total += 1;
		}else if(old.point > now.point){
			worse++;
//			total += now.point/old.point;
		}else{
//			total += 1;
		}
		System.err.flush();
	}
	
}

class Points implements Comparable<Points>{
	int seed;
	double point;
	public Points(int seed, double point){
		this.seed = seed;
		this.point = point;
	}
	
	public String toString(){
		return seed+" "+point;
	}
	
	@Override
	public int compareTo(Points o) {
		return seed - o.seed;
	}
}

class ContestWriter {
	private PrintWriter out;

	public ContestWriter(String filename) throws IOException {
		out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	}

	public ContestWriter() throws IOException {
		out = new PrintWriter(System.out);
	}

	public void println(String str) {
		out.println(str);
	}

	public void print(String str) {
		out.print(str);
	}

	public void close() {
		out.close();
	}
}

class ContestScanner {
	private BufferedReader reader;
	private String[] line;
	private int idx;

	public ContestScanner() throws FileNotFoundException {
		reader = new BufferedReader(new InputStreamReader(System.in));
	}

	public ContestScanner(String filename) throws FileNotFoundException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				filename)));
	}
	
	public void close() throws IOException{
		reader.close();
	}

	public String nextToken() throws IOException {
		if (line == null || line.length <= idx) {
			line = reader.readLine().trim().split(" ");
			idx = 0;
		}
		return line[idx++];
	}

	public long nextLong() throws IOException, NumberFormatException {
		return Long.parseLong(nextToken());
	}

	public int nextInt() throws NumberFormatException, IOException {
		return (int) nextLong();
	}

	public double nextDouble() throws NumberFormatException, IOException {
		return Double.parseDouble(nextToken());
	}
}
