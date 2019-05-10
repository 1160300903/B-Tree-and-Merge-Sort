import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MergeSort {
	public static int recordPerBlock = 5000;
	public static int maxRecordPerWay = 60000;
	public static int maxBlock = 12;
	public static Comparator<DataNode> comparator = new Comparator<DataNode>() {
		@Override
        public int compare(DataNode arg0, DataNode arg1) {
			int key0 = arg0.key;
			int key1 = arg1.key;
            if(key0>key1)
            	return  1;
            else if(key0==key1)
            	return 0;
            return -1;
        }
    };
    public static void main(String[] args) {
    	List<Integer> a = new ArrayList<>(Arrays.asList(1,2,3,4));
    	System.out.println(a.remove(0));
    	MergeSort ms = new MergeSort();
    	ms.mergeSort();
    	try {
			BufferedReader br = new BufferedReader(new FileReader(new File("sorted_data.txt")));
			String line = null;
			for(int i=0;i<1000000;i++) {
				line = br.readLine();
				String[] strings = line.split(" ");
				assert Integer.parseInt(strings[0])==i;
			}
			assert br.readLine()==null;
			System.out.println("判断完成");
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	/*
	 * 内存大小为1m
	 * 每条记录16B
	 * 故内存最多可以装下65536条记录
	 * 据以上数据，设一路有60000条记录，一块有5000条记录，每一路由12块组成。
	 * 内存至少装入12块数据
	 * 一下实验根据上述假设完成
	 */
	public void mergeSort() {
		//初始化虚拟的磁盘
		VirtualDisk allRecord = new VirtualDisk("data.txt");
		List<VirtualDisk> ways = new ArrayList<VirtualDisk>();
		//实现分组和分组排序
		List<DataNode> current = new ArrayList<DataNode>();
		int count = 0;
		while(!allRecord.isEnd()) {
			current.addAll(allRecord.readBlock());
			count+=1;
			if(count==MergeSort.maxBlock) {
				count = 0;
				Collections.sort(current,comparator);
				ways.add(new VirtualDisk(current));
				current.clear();
			}
		}
		//将剩余不足12块的记录归为一路
		if(count!=0) {
			count = 0;
			Collections.sort(current,comparator);
			ways.add(new VirtualDisk(current));
		}
		allRecord.clear();
		//进行归并排序
		while(ways.size()>1) {
			int emptyWays = 0;
			int numOfMergeWays = Math.min(MergeSort.maxBlock-2, ways.size());
			int[] wayTag = new int[numOfMergeWays];
			//设置排序块和输出块和每一路的块
			List<DataNode> outputBlock = new ArrayList<DataNode>();
			VirtualDisk mergeResult = new VirtualDisk(Arrays.asList());
			List<ArrayList<DataNode>> inputBlocks =new ArrayList<ArrayList<DataNode>>(numOfMergeWays);
			DataNode[] sortBlock = new DataNode[numOfMergeWays];
			
			for(int i=0;i<numOfMergeWays;i++) {
				inputBlocks.add(ways.get(i).readBlock());
				sortBlock[i] = inputBlocks.get(i).remove(0);
				wayTag[i] = 0;
			}
			
			while(emptyWays<numOfMergeWays) {
				DataNode min = new DataNode(Integer.MAX_VALUE,"");
				int index = -1;
				for(int i=0;i<numOfMergeWays;i++)
					if (wayTag[i]==0&&sortBlock[i].key<min.key){
						min = sortBlock[i];
						index = i;
					}
				outputBlock.add(min);
				if(outputBlock.size()==MergeSort.recordPerBlock) {
					mergeResult.addAll(outputBlock);
					outputBlock.clear();
				}
				if(inputBlocks.get(index).isEmpty()) {
					if(ways.get(index).isEnd()) {
						emptyWays++;
						wayTag[index] = 1;
					}
					else {
						inputBlocks.get(index).addAll(ways.get(index).readBlock());
						sortBlock[index] = inputBlocks.get(index).remove(0);
					}
				}
				else {
					sortBlock[index] = inputBlocks.get(index).remove(0);
				}
			}
			if(outputBlock.size()!=0) {
				mergeResult.addAll(outputBlock);
				outputBlock.clear();
			}
			for(int i=0;i<numOfMergeWays;i++)
				ways.remove(0);
			ways.add(mergeResult);
		}
		outputToRealDisk(ways.get(0));
	}
	private void outputToRealDisk(VirtualDisk data) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("sorted_data.txt")));
			while(!data.isEnd()) {
				List<DataNode> dn = data.readBlock();
				for(DataNode d:dn)
					bw.write(d.key+" "+d.data+"\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
class VirtualDisk{
	List<DataNode> memory = new ArrayList<DataNode>();
	int count = 0;//下一个要读的块数
	VirtualDisk(String path){
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line = null;
			while((line = br.readLine())!=null) {
				String[] strings = line.split(" ");
				memory.add(new DataNode(Integer.parseInt(strings[0]),strings[1]));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert memory.size()<MergeSort.maxRecordPerWay;
	}
	VirtualDisk(List<DataNode> a){
		assert a.size()<MergeSort.maxRecordPerWay;
		for(DataNode s:a)
			memory.add(s);
	}
	public ArrayList<DataNode> readBlock() {
		if(isEnd())
			return null;
		int begin = count*MergeSort.recordPerBlock;
		int end = Math.min(begin+MergeSort.recordPerBlock, memory.size());
		ArrayList<DataNode> result = new ArrayList<>();
		for(int i=begin;i<end;i++) {
			result.add(memory.get(i));
		}
		count++;
		return result;
	}
	public boolean isEnd() {
		if(count*MergeSort.recordPerBlock>=memory.size())
			return true;
		return false;
	}
	public void clear() {
		memory = new ArrayList<DataNode>();
	}
	public void addAll(List<DataNode> newData) {
		memory.addAll(newData);
	}
}
