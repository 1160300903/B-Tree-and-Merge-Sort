

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataGenerator {
	public static void main(String[] args) {
		//System.out.println(DataGenerator.getRandStr(12).getBytes().length);
		DataGenerator.genetrateData();
	}
	public static void genetrateData() {
		 try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt", false));
			List<String> data = new ArrayList<String>();
			for(int i=0;i<20;i++) {
				data.add(i+" "+getRandStr(12)+"\n");
			}
			Collections.shuffle(data);
			for(String s:data)
				writer.write(s);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 public static String getRandStr(int num){
			String strs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			StringBuffer buff = new StringBuffer();
			for(int i=1;i<=num;i++){
				char str = strs.charAt((int)(Math.random() * 26));
				buff.append(str);
			}
			return buff.toString();
	   }
}