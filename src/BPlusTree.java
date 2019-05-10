

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class BPlusTree {
	public static int count = 0;
	public static void main(String[] args) {
		/*BPlusTree bpt = new BPlusTree(4,new LinkedList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9,10
				,11,12)),new LinkedList<String>(Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l")));*/
		BPlusTree bpt = new BPlusTree(4,"data.txt");
		System.out.println(bpt.toString());
		Scanner sc = new Scanner(System.in);
		int choice = 0;
		int key = -1;
		String data = null;
		a:while(true) {
			System.out.println("0.quit");
			System.out.println("1.search");
			System.out.println("2.insert");
			System.out.println("3.delete");
			choice = sc.nextInt();
			sc.nextLine();
			switch(choice) {
			case 0:
				break a;
			case 1:
				System.out.println("Please input the key you wanna search");
				key = sc.nextInt();
				sc.nextLine();
				System.out.println(bpt.searchValue(key));
				System.out.println("----------------------cut line------------------------");
				break;
			case 2:
				System.out.println("Please input the key and data");
				key = sc.nextInt();
				data = sc.nextLine();
				bpt.insert(key, data);
				System.out.println(bpt.toString());
				System.out.println("----------------------cut line------------------------");
				break;
			case 3:
				System.out.println("Please input the key you wanna delete");
				key = sc.nextInt();
				sc.nextLine();
				bpt.delete(key);
				System.out.println(bpt.toString());
				System.out.println("----------------------cut line------------------------");
				break;
			}
		}
		sc.close();
	}
	int sonNum;
	Node root;
	BPlusTree(int sonNum,List<Integer> keys,List<String> datas){
		assert ((datas.size()+1)/sonNum)>0.5;
		assert keys.size()==datas.size();
		this.sonNum = sonNum;
		root = new LeafNode(sonNum-1,null);
		for(int i=0;i<keys.size();i++)
			this.insert(keys.get(i),datas.get(i));
	}
	BPlusTree(int sonNum,String path){
		List<Integer> keys = new ArrayList<Integer>();
		List<String> datas = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line = null;
			while((line=br.readLine())!=null) {
				String[] keyAndValue = line.split(" ");
				keys.add(Integer.parseInt(keyAndValue[0]));
				datas.add(keyAndValue[1]);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assert ((datas.size()+1)/sonNum)>0.5;
		assert keys.size()==datas.size();
		this.sonNum = sonNum;
		root = new LeafNode(sonNum-1,null);
		for(int i=0;i<keys.size();i++)
			this.insert(keys.get(i),datas.get(i));
	}
	@Override
	public String toString() {
		BPlusTree.count=0;
		StringBuffer sb = new StringBuffer();
		Queue<Node> q = new LinkedList<Node>();
		q.add(root);
		while(!q.isEmpty()) {
			Node current = q.remove();
			current.count = BPlusTree.count;
			BPlusTree.count++; 
			if(current instanceof InternalNode) {
				for(Node n:((InternalNode) current).son) {
					q.add(n);
				}
			}
		}
		q.add(root);
		while(!q.isEmpty()) {
			Node current = q.remove();
			sb.append(current.toString());
			if(current instanceof InternalNode) {
				for(Node n:((InternalNode) current).son) {
					q.add(n);
				}
			}
		}
		return sb.toString();
	}
	//从左向右查找，找到第一个比key大的键它左边的指针指向根节点
	/**
	 * 查找叶节点
	 * @param key 查找的键
	 * @return 键应该在的叶节点
	 */
	private LeafNode searchLeaf(int key) {
		Node current = root;
		while(current instanceof InternalNode) {
			InternalNode temp = (InternalNode)current;
			int loc = temp.son.size()-1;
			for(int i=0;i<temp.key.size();i++) {
				if(key<temp.key.get(i)) {
					loc = i;
					break;
				}
			}
			current = temp.getSon(loc);
		}
		LeafNode temp = (LeafNode)current;
		return temp;
	}
	/**
	 * 查找数据
	 * @param key 数据的键
	 * @return 数据
	 */
	public String searchValue(int key) {
		LeafNode temp = this.searchLeaf(key);
		DataNode result = null;
		for(DataNode d:temp.data)
			if (d.key==key) {
				result = d;
				break;
			}
		return result.data;
	}
	/**
	 * 在B+树中插入键和值
	 * @param key 要插入的键
	 * @param data 要插入的值
	 */
	public void insert(int key,String data) {
		LeafNode current = this.searchLeaf(key);
		NodeKey nk = current.insert(key, data);
		InternalNode fatherNode = current.father;
		while(nk!=null&&fatherNode!=null) {
			Node newSon = nk.n;
			nk = fatherNode.insert(nk.key, newSon);
			fatherNode = fatherNode.father;
		}
		if(nk!=null&&fatherNode==null) {
			InternalNode newRoot= new InternalNode(this.sonNum,null);
			newRoot.son.add(this.root);
			this.root.father=newRoot;
			newRoot.key.add(nk.key);
			newRoot.son.add(nk.n);
			nk.n.father = newRoot;
			
			this.root=newRoot;
		}
		//System.out.println(key);
		//System.out.println(this.toString());
	}
	/**
	 * 在树中删除元素
	 * @param key 要删除的键
	 * @return 键对应的值。如果键不在树中返回null
	 */
	public void delete(int key) {
		Node result;
		LeafNode current = this.searchLeaf(key);
		result = current.delete(key);
		if(result!=null)
			this.root = result;
	}
}
class NodeKey{
	Node n;
	int key;
	NodeKey(Node n, int i){
		this.n = n;
		this.key = i;
	}
}
class DataNode{
	int key;
	String data;
	DataNode(int key,String data){
		this.key = key;
		this.data = data;
	}
}
abstract class Node{
	InternalNode father;
	int count;
	abstract boolean isNearUnder();
	abstract void merge(Node n);
	int getIndex() {
		int index = -1;
		InternalNode f = this.father;
		for(int i=0;i<f.son.size();i++)
			if(f.son.get(i)==this) {
				index = i;
				break;
			}
		return index;
	}
}



class InternalNode extends Node{
	List<Integer> key;
	List<Node> son;
	int sonNum;
	InternalNode(int n,InternalNode father){
		key = new LinkedList<>();
		son = new LinkedList<>();
		this.father = father;
		sonNum = n;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("内节点"+count+" ");
		sb.append("   键值[");
		for(int k:key) {
			sb.append(k+" ");
		}
		sb.append("]  儿子[");
		for(Node n:son) {
			sb.append(n.count+" ");
		}
		if(father!=null)
		sb.append("]  父节点"+father.count+"\n");
		else
			sb.append("]\n");
		return sb.toString();
	}
	public NodeKey insert(int key,Node newSon) {
		if(this.isFull()) {
			int loc = this.key.size();
			for(int i=0;i<this.key.size();i++)
				if(key<this.key.get(i)) {
					loc = i;
					break;
				}
			this.key.add(loc, key);
			this.son.add(loc+1,newSon);
			newSon.father = this;
					
			int median = this.sonNum/2;
			InternalNode newNode = new InternalNode(this.sonNum,this.father);
			int full_size = this.key.size();
			int medianKey = this.key.remove(median);
			Node temp = this.son.remove(median+1);
			newNode.son.add(temp);
			temp.father = newNode;
			for(int i=median+1;i<full_size;i++) {
				newNode.key.add(this.key.remove(median));
				temp = this.son.remove(median+1);
				newNode.son.add(temp);
				temp.father=newNode;
				
			}
			return new NodeKey(newNode,medianKey);
		}
		else {
			int loc = this.key.size();
			for(int i=0;i<this.key.size();i++)
				if(key<this.key.get(i)) {
					loc = i;
					break;
				}
			this.key.add(loc, key);
			this.son.add(loc+1,newSon);
			newSon.father = this;
			return null;
		}
	}
	/**
	 * 删除指定位置的索引
	 * @param newIndex 要删除的索引的位置，一并删除相同位置的指针
	 */
	public Node delete(int newIndex) {
		boolean loop = this.isNearUnder();
		Node result = null;
		this.key.remove(newIndex);
		this.son.remove(newIndex+1);
		if(father!=null&&loop) {
			int index = this.getIndex();	
			if(index>0&&!father.son.get(index-1).isNearUnder()) {
				InternalNode leftsib = (InternalNode)father.son.get(index-1);
				int modifiedKey = leftsib.key.remove(leftsib.key.size()-1);
				Node modifiedNode = leftsib.son.remove(leftsib.son.size()-1);
				this.key.add(0,father.key.get(index-1));
				this.son.add(0,modifiedNode);
				modifiedNode.father = this;
				father.modifyIndex(modifiedKey, index-1);
			}
			else if(index<father.son.size()-1&&!father.son.get(index+1).isNearUnder()) {
				InternalNode rightsib = (InternalNode)father.son.get(index+1);
				int modifiedKey = rightsib.key.remove(0);
				Node modifiedNode = rightsib.son.remove(0);
				this.key.add(father.key.get(index));
				this.son.add(modifiedNode);
				modifiedNode.father = this;
				father.modifyIndex(modifiedKey, index);
			}else if(index>0) {
				InternalNode leftsib = (InternalNode)father.son.get(index-1);
				leftsib.merge(this);
				result = father.delete(index-1);
			}else if(index<father.son.size()-1) {
				this.merge((InternalNode)father.son.get(index+1));
				result = father.delete(index);
			}
		}
		if(father==null&&key.size()==0) {
			result = son.get(0);
		}
		return result;
	}
	/**
	 * 修改节点的索引
	 * @param key 新的索引值
	 * @param index 修改的位置
	 */
	void modifyIndex(int key,int index) {
		this.key.set(index, key);
	}
	public boolean isFull() {
		if(this.son.size()==this.sonNum)
			return true;
		return false;
	}
	@Override
	public boolean isNearUnder() {
		if((this.son.size()-1)<this.sonNum*0.5)
			return true;
		return false;
	}
	public Node getSon(int i) {
		return this.son.get(i);
	}
	public InternalNode getFather() {
		return father;
	}
	public int getKey(int i) {
		return this.key.get(i);
	}
	@Override
	void merge(Node n) {
		assert n instanceof InternalNode;
		InternalNode i = (InternalNode)n;
		int index = i.getIndex();
		int newKey = i.father.key.get(index-1);
		this.key.add(newKey);
		Iterator<Integer> keyIt = i.key.iterator();
		Iterator<Node> nodeIt = i.son.iterator();
		Node newSon = nodeIt.next();
		this.son.add(newSon);
		newSon.father=this;//node比索引多一个
		while(keyIt.hasNext()) {
			this.key.add(keyIt.next());
			newSon = nodeIt.next();
			this.son.add(newSon);
			newSon.father=this;
		}
	}
}
class LeafNode extends Node{
	List<Integer> key;
	List<DataNode> data;
	LeafNode next;
	int dataNum;
	LeafNode(int n,InternalNode father){
		key = new LinkedList<>();
		data = new LinkedList<>();
		this.father = father;
		dataNum = n;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("叶节点"+count+" ");
		sb.append("  键值[");
		for(int k:key) {
			sb.append(k+" ");
		}
		sb.append("]  数值[");
		for(DataNode s:data) {
			sb.append(s.data+" ");
		}
		sb.append("]  ");
		if(next!=null)
			sb.append("后继"+next.count+"  ");
		else
			sb.append("无后继   ");
		if(father!=null)
		sb.append("父节点"+father.count+"\n"); 
		else
			sb.append("无父节点");
		return sb.toString();
	}
	public Node delete(int key) {
		Node result = null;
		Iterator<DataNode> it = this.data.iterator();
		Iterator<Integer> keyIt = this.key.iterator();
		boolean loop = this.isNearUnder();
		boolean delete = false;
		while(it.hasNext()) {
			DataNode d = it.next();
			keyIt.next();
			if(d.key==key) {
				it.remove();
				keyIt.remove();
				delete = true;
				break;
			}
		}
		if(!delete) {
			System.out.println("无效的键");
			return result;
		}
		if(loop) {
			InternalNode f = this.father;
			int index = this.getIndex();	
			if(index>0&&!f.son.get(index-1).isNearUnder()) {
				LeafNode leftsib = (LeafNode)f.son.get(index-1);
				int modifiedKey = leftsib.key.remove(leftsib.key.size()-1);
				DataNode modifiedData = leftsib.data.remove(leftsib.data.size()-1);
				this.key.add(0,modifiedKey);
				this.data.add(0,modifiedData);
				f.modifyIndex(modifiedKey, index-1);
			}
			else if(index<f.son.size()-1&&!f.son.get(index+1).isNearUnder()) {
				LeafNode rightsib = (LeafNode)f.son.get(index+1);
				int modifiedKey = rightsib.key.remove(0);
				DataNode modifiedData = rightsib.data.remove(0);
				this.key.add(modifiedKey);
				this.data.add(modifiedData);
				f.modifyIndex(rightsib .data.get(0).key, index);
			}else if(index>0) {
				LeafNode leftsib = (LeafNode)f.son.get(index-1);
				leftsib.merge(this);
				result = father.delete(index-1);
			}else if(index<f.son.size()-1) {
				this.merge(this.next); 
				result = father.delete(index);
			}
		}
		return result;
	}
	public NodeKey insert(int key,String data){
		if(this.isFull()) {
			int loc = this.key.size();
			for(int i=0;i<this.key.size();i++)
				if(key<this.key.get(i)) {
					loc = i;
					break;
				}
			this.key.add(loc, key);
			this.data.add(loc,new DataNode(key,data));
			
			int median = (this.dataNum+1)/2;
			LeafNode newNode = new LeafNode(this.dataNum,this.father);
			int full_size = this.key.size();
			for(int i=median;i<full_size;i++) {
				newNode.key.add(this.key.get(median));
				this.key.remove(median);
				newNode.data.add(this.data.get(median));
				this.data.remove(median);
			}
			newNode.next = this.next;
			this.next = newNode;
			return new NodeKey(newNode,newNode.key.get(0));
		}
		else {
			int loc = this.key.size();
			for(int i=0;i<this.key.size();i++)
				if(key<this.key.get(i)) {
					loc = i;
					break;
				}
			this.key.add(loc, key);
			this.data.add(loc,new DataNode(key,data));
			return null;
		}
	}
	public boolean isFull() {
		if(this.data.size()==this.dataNum)
			return true;
		return false;
	}
	@Override
	public boolean isNearUnder() {
		if((this.data.size()-1)<(this.dataNum)*0.5)
			return true;
		return false;
	}
	public DataNode getSon(int i) {
		return this.data.get(i);
	}
	public InternalNode getFather() {
		return father;
	}
	public int getKey(int i) {
		return this.key.get(i);
	}
	@Override
	void merge(Node n) {
		assert n instanceof LeafNode;
		LeafNode l = (LeafNode)n;
		this.next = l.next;
		Iterator<Integer> keyIt = l.key.iterator();
		Iterator<DataNode> dataIt = l.data.iterator();
		while(keyIt.hasNext()) {
			this.key.add(keyIt.next());
			this.data.add(dataIt.next());
		}
	}
}
