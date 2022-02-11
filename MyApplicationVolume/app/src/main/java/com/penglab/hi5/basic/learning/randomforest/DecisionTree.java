package com.penglab.hi5.basic.learning.randomforest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

public class DecisionTree {
    private static final int INDEX_SKIP = 2;
    private static final int MIN_SIZE_TO_CHECK_EACH = 10;
    private static final int MIN_NODE_SIZE = 5;

    private int N;//记录的数据个数
    private int testN;  //the number of samples left out of the boostrap of all N to test error rate
    private int correct;//Of the testN, the number that were correctly identified

    private int[] importance;  // an estimate of the importance of each attribute in the data record

    private TreeNode root;    // This is the root of the Decision Tree

    /** This is a pointer to the Random Forest this decision tree belongs to */
    private RandomForest forest;
    /** This keeps track of all the predictions done by this tree */
    public ArrayList<Integer> predictions;

    private class TreeNode implements Cloneable{
        public boolean isLeaf;
        public TreeNode left;
        public TreeNode right;
        public int splitAttributeM;
        public Integer Class;
        public List<float[]> data;
        public float splitValue;
        public int generation;
        public ArrayList<Integer> attrArr;//随机从M个属性选取Ms个属性，Ms= log2（M），存的是其属性的序数

        public TreeNode(){
            splitAttributeM = -99;
            splitValue = -99;
            generation = 1;
        }

        public TreeNode clone(){
            TreeNode treeCopy = new TreeNode();
//            treeCopy = (TreeNode) super.clone();
            treeCopy.data = null;
            treeCopy.isLeaf = isLeaf;
            if(left != null){
                treeCopy.left = left.clone();
            }
            if(right != null){
                treeCopy.right = right.clone();
            }
            treeCopy.splitAttributeM = splitAttributeM;
            treeCopy.Class = Class;
            treeCopy.splitValue = splitValue;
            return treeCopy;
        }
    }

    public boolean saveDecisionTree(File dir, int i){
        String path = dir + "/DecisionTree_" + String.valueOf(i) + ".dt";
        try {
            File f = new File(path);
            if(!f.exists()){
                f.createNewFile();
            }
            FileOutputStream fid = new FileOutputStream(f);
            OutputStreamWriter writer = new OutputStreamWriter(fid, "UTF-8");
//            writer.append("#name \n");
//            writer.append("#comment \n");
            writer.append("##n,left,right,isLeaf,splitAttributeM,Class,splitValue,generation,attrArr\n");
            Queue<TreeNode> queue = new LinkedList<>();
            queue.offer(root);
            Vector<TreeNode> treeNodes = new Vector<>();
            treeNodes.add(root);

            while (!queue.isEmpty()){
                TreeNode temp = queue.poll();
                if(temp.left != null){
                    treeNodes.add(temp.left);
                    queue.add(temp.left);
                }
                if(temp.right != null){
                    treeNodes.add(temp.right);
                    queue.add(temp.right);
                }
            }

            Map<TreeNode,Integer> treeNodeIndexMap = new HashMap<>();
            for(int k=0; k<treeNodes.size(); k++){
                treeNodeIndexMap.put(treeNodes.get(k),k);
            }

            queue.offer(root);

            while (!queue.isEmpty()){
                TreeNode temp = queue.poll();
                int n = treeNodeIndexMap.get(temp);
                int left = -1, right = -1;
                if(treeNodeIndexMap.get(temp.left)!=null){
                    left = treeNodeIndexMap.get(temp.left);
                }
                if(treeNodeIndexMap.get(temp.right)!=null){
                    right = treeNodeIndexMap.get(temp.right);
                }
                int leaf = temp.isLeaf?1:0;
                int c = temp.Class == null? -1:temp.Class;

                writer.append(Integer.toString(n)).append(" ").append(Integer.toString(left)).append(" ")
                        .append(Integer.toString(right)).append(" ").append(Integer.toString(leaf)).append(" ")
                        .append(Integer.toString(temp.splitAttributeM)).append(" ")
                        .append(Integer.toString(c)).append(" ")
                        .append(Float.toString(temp.splitValue)).append(" ")
                        .append(Integer.toString(temp.generation));
                if(temp.attrArr != null){
                    for(int k=0; k<temp.attrArr.size(); k++){
                        writer.append(" ").append(Integer.toString(temp.attrArr.get(k)));
                    }
                }

                writer.append("\n");

                if(temp.left != null){
                    queue.add(temp.left);
                }
                if(temp.right != null){
                    queue.add(temp.right);
                }
            }
            writer.close();
            fid.close();
        } catch (IOException e) {
            System.out.println("saveDecisionTree Exception "+e.getMessage());
            return false;
        }
        System.out.println("done with saving file: "+path);
        return true;
    }

    public boolean readDecisionTree(String file){
        ArrayList<String> arrayList = new ArrayList<String>();
        System.out.println("in read DecisionTree");
        try {
            File f = new File(file);
            FileInputStream fid = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fid);
            BufferedReader br = new BufferedReader(isr);
            String str;
            while ((str = br.readLine()) != null) {
                arrayList.add(str);
            }
            br.close();
            isr.close();
        } catch (Exception e) {
            System.out.println("dtReaderException" + e.getMessage());
            return false;
        }

        Vector<TreeNode> treeNodes = new Vector<>();
        for(int i=0; i<arrayList.size(); i++){
            String current = arrayList.get(i);
            String[] s = current.split(" ");
            if (s[0].substring(0, 1).equals("#")) continue;
            TreeNode temp = new TreeNode();
            temp.isLeaf = s[3].equals("1");
            temp.splitAttributeM = Integer.parseInt(s[4]);
            temp.Class = Integer.parseInt(s[5]);
            temp.splitValue = Float.parseFloat(s[6]);
            temp.generation = Integer.parseInt(s[7]);
            if(s.length>8){
                temp.attrArr = new ArrayList<>();
                for(int j=8; j<s.length; j++){
                    temp.attrArr.add(Integer.parseInt(s[j]));
                }
            }
            treeNodes.add(temp);
        }
        root = treeNodes.get(0);

        for(int i=0; i<treeNodes.size(); i++) {
            TreeNode temp = treeNodes.get(i);
            String current = arrayList.get(i + 1);
            String[] s = current.split(" ");
            int left = Integer.parseInt(s[1]);
            int right = Integer.parseInt(s[2]);
            if (left > 0) {
                temp.left = treeNodes.get(left);
            }
            if (right > 0) {
                temp.right = treeNodes.get(right);
            }
        }

        return true;
    }

    private class DoubleWarp{
        public double d;
        public DoubleWarp(double d){
            this.d = d;
        }
    }

    /**
     * This constructs a decision tree from a data matrix.
     * It first creates a bootstrap sample, the train data matrix, as well as the left out records,
     * the test data matrix. Then it creates the tree, then calculates the variable importance (not essential)
     * and then removes the links to the actual data (to save memory)
     *
     * @param data		The data matrix as a List of int arrays - each array is one record,
     *                  each index in the array is one attribute, and the last index is the class
     * 					(ie [ x1, x2, . . ., xM, Y ]).
     * @param forest	The random forest this decision tree belongs to
     * @param num	    the Tree number
     */
    public DecisionTree(ArrayList<float[]> data, RandomForest forest, int num){
        this.forest = forest;
        N = data.size();
        importance = new int[this.forest.M];
        predictions = new ArrayList<Integer>();

        //System.out.println("Make a DecisionTree num : "+num+" with N:"+N+" M:"+RandomForest.M+" Ms:"+RandomForest.Ms);

        ArrayList<float[]> train = new ArrayList<>(N); //data becomes the "bootstrap" - that's all it knows
        ArrayList<float[]> val = new ArrayList<>();
        //System.out.println("Creating tree No."+num);
        BootStrapSample(data, train, val, num);//populates train and test using data
        testN = val.size();
        correct = 0;

        root = CreateTree(train, num);//creating tree using training data
        CalcTreeVariableImportanceAndError(val, num);
        FlushData(root);
    }

    public DecisionTree(){

    }

    /**
     * Responsible for gauging the error rate of this tree and
     * calculating the importance values of each attribute
     *
     * @param val	The left out data matrix
     * @param nv    The Tree number
     */
    private void CalcTreeVariableImportanceAndError(ArrayList<float[]> val, int nv) {
        //calculate error rate
        correct = CalcTreeErrorRate(val, nv);//the num of correct prediction record
        CalculateClasses(val, nv);
        //calculate importance of each attribute
        for (int m=0; m<this.forest.M; m++){
            ArrayList<float[]> test_data = RandomlyPermuteAttribute(CloneData(val), m);
            int correctAfterPermute = 0;
            for (float[] arr:test_data){
                int pre_label = Evaluate(arr);
                if (pre_label == GetClass(arr))
                    correctAfterPermute++;
            }
            importance[m] += (correct - correctAfterPermute);
        }
        System.out.println("The importance of tree " + nv + " as follows");
//		for(int m=0; m<importance.length; m++){
//			System.out.println(" Attr" + m + ":" + importance[m]);
//		}
    }

    /**
     * Calculates the tree error rate,
     * displays the error rate to console,
     * and updates the total forest error rate
     *
     * @param val	the left out test data matrix
     * @param nu    The Tree number
     * @return	the number correct
     */
    public int CalcTreeErrorRate(ArrayList<float[]> val, int nu){
        int correct = 0;
        for (float[] record:val){
            int pre_label = Evaluate(record);
            forest.UpdateOOBEstimate(record, pre_label);
            int actual_label = (int) record[record.length-1];//actual_label
            if (pre_label == actual_label)
                correct++;
        }

        double err = 1 - correct/((double)val.size());
//		System.out.print("\n");
        System.out.println("Number of correct  = " + correct + ", out of :" + val.size());
        System.out.println("Test-Data error rate of tree " + nu + "  is: " + (err * 100) + " %");
        return correct;
    }
    /**
     * This method will get the classes and will return the updates
     * @param val	The left out data matrix
     * @param nu    The Tree number
     */
    public ArrayList<Integer> CalculateClasses(ArrayList<float[]> val, int nu){
        ArrayList<Integer> preList = new ArrayList<Integer>();
        int korect = 0;
        for(float[] record : val){
            int pre_label = Evaluate(record);
            preList.add(pre_label);
            int actual_label = (int) record[record.length-1];
            if (pre_label==actual_label)
                korect++;
        }
        predictions = preList;
        return preList;

    }
    /**
     * This will classify a new data record by using tree
     * recursion and testing the relevant variable at each node.
     *
     * This is probably the most-used function in all of <b>GemIdent</b>.
     * It would make sense to inline this in assembly for optimal performance.
     *
     * @param record 	the data record to be classified
     * @return			the class the data record was classified into
     */
    public int Evaluate(float[] record){
        TreeNode evalNode = root;

        while (true){
            if (evalNode.isLeaf)
                return evalNode.Class;
            if (record[evalNode.splitAttributeM] <= evalNode.splitValue)
                evalNode = evalNode.left;
            else
                evalNode = evalNode.right;
        }
    }
    /**
     * Takes a list of data records, and switches the m-th attribute across data records.
     * This is important in order to test the importance of the attribute. If the attribute
     * is randomly permuted and the result of the classification is the same, the attribute is
     * not important to the classification and vice versa.
     *
     * @see <a href="http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#varimp">Variable Importance</a>
     * @param val		The left out data matrix to be permuted
     * @param m			The attribute index to be permuted
     * @return			The data matrix with the m-th column randomly permuted
     */
    private ArrayList<float[]> RandomlyPermuteAttribute(ArrayList<float[]> val, int m){
        int num = val.size() * 2;
        for (int i=0; i<num; i++){
            int a = (int)Math.floor(Math.random() * val.size());
            int b = (int)Math.floor(Math.random() * val.size());
            float[] arrA = val.get(a);
            float[] arrB = val.get(b);
            float temp = arrA[m];
            arrA[m] = arrB[m];
            arrB[m] = temp;
        }
        return val;
    }
    /**
     * Creates a copy of the data matrix
     * @param data		the data matrix to be copied
     * @return			the cloned data matrix
     */
    private ArrayList<float[]> CloneData(ArrayList<float[]> data){
        ArrayList<float[]> clone=new ArrayList<>(data.size());
        if(data.isEmpty()){
            return clone;
        }
        int M=data.get(0).length;
        for (int i=0;i<data.size();i++){
            float[] arr=data.get(i);
            float[] arrClone=new float[M];
            for (int j=0;j<M;j++){
                arrClone[j]=arr[j];
            }
            clone.add(arrClone);
        }
        return clone;
    }
    /**
     * This creates the decision tree according to the specifications of random forest trees.
     *
     * @see <a href="http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#overview">Overview of random forest decision trees</a>
     * @param train		the training data matrix (a bootstrap sample of the original data)
     * @param ntree     the tree number
     * @return			the TreeNode object that stores information about the parent node of the created tree
     */
    private TreeNode CreateTree(ArrayList<float[]> train, int ntree){
        TreeNode root = new TreeNode();
        root.data = train; // public List<int[]> data;
        //System.out.println("creating ");
//        ArrayList<Integer> vars = GetVarsToInclude();//randomly selects Ms' index of attributes from M
//        root.attrArr = vars;
        RecursiveSplit(root, ntree);
        return root;
    }

    private class DoubleWrap{
        public double d;
        public DoubleWrap(double d){
            this.d=d;
        }
    }
    /**
     * This is the crucial function in tree creation.
     *
     * <ul>
     * <li>Step A
     * Check if this node is a leaf, if so, it will mark isLeaf true
     * and mark Class with the leaf's class. The function will not
     * recurse past this point.
     * </li>
     * <li>Step B
     * Create a left and right node and keep their references in
     * this node's left and right fields. For debugging purposes,
     * the generation number is also recorded. The {@link RandomForest#Ms Ms} attributes are
     * now chosen by the {@link #GetVarsToInclude() GetVarsToInclude} function
     * </li>
     * <li>Step C
     * For all Ms variables, first {@link #SortAtAttribute(List,int) sort} the data records by that attribute,
     * then look through the values from lowest to highest.
     * If value i is not equal to value i+1, record i in the list of "indicesToCheck."
     * This speeds up the splitting. If the number of indices in indicesToCheck >  MIN_SIZE_TO_CHECK_EACH
     * then we will only {@link #CheckPosition(int, int, int, DoubleWrap, TreeNode, int) check} the
     * entropy at every {@link #INDEX_SKIP INDEX_SKIP} index otherwise,
     * we {@link #CheckPosition(int, int, int, DoubleWrap, TreeNode, int) check}
     * the entropy for all. The "E" variable records the entropy and we are trying to find the minimum in which to split on
     * </li>
     * <li>Step D
     * The newly generated left and right nodes are now checked:
     * If the node has only one record, we mark it as a leaf and set its class equal to the class of the record.
     * If it has less than {@link #MIN_NODE_SIZE MIN_NODE_SIZE} records,
     * then we mark it as a leaf and set its class equal to the {@link #GetMajorityClass(List) majority class}.
     * If it has more, then we do a manual check on its data records and if all have the same class, then it
     * is marked as a leaf. If not, then we run {@link #RecursiveSplit(TreeNode, int) RecursiveSplit} on
     * that node
     * </li>
     * </ul>
     *
     * @param parent	The node of the parent
     * @param Ntreenum  the tree number
     */
    private void RecursiveSplit(TreeNode parent, int Ntreenum){
        //System.out.println("Recursivly spilitting tree : "+Ntreenum);
        if (!parent.isLeaf){
            //-------------------------------Step A
            //当前结点包含的样本全属于同一类别，无需划分;
            Integer Class = CheckIfLeaf(parent.data);
            if (Class != null){
                parent.isLeaf = true;
                parent.Class = Class;
                //System.out.println("leaf for this tree: "+Ntreenum);
//				System.out.print("parent leaf! Class:"+parent.Class+"  ");
//				PrintOutClasses(parent.data);
                return;
            }

            //-------------------------------Step B
            int Nsub = parent.data.size();
//			PrintOutClasses(parent.data);

            ArrayList<Integer> vars = GetVarsToInclude();//randomly selects Ms' index of attributes from M
            parent.attrArr = vars;

            parent.left = new TreeNode();
            parent.left.generation = parent.generation + 1;
            parent.right = new TreeNode();
            parent.right.generation = parent.generation + 1;

            DoubleWrap lowestE = new DoubleWrap(Double.MIN_VALUE);

            //假如当前属性集为空，返回样本数最多的类;
            if(parent.attrArr.size() == 0){
                parent.isLeaf = true;
                parent.Class = GetMajorityClass(parent.data);
                return;
            }
            //-------------------------------Step C
            //所有样本在所有属性上取值相同，无法划分，返回样本数最多的类;
            int sameClass = 0;
            for (int m:parent.attrArr){
                SortAtAttribute(parent.data, m);//sorts on a particular column in the row
                ArrayList<Integer> indicesToCheck = new ArrayList<Integer>();
                for (int n=1; n<Nsub; n++){
                    int classA = GetClass(parent.data.get(n-1));
                    int classB = GetClass(parent.data.get(n));
                    if (classA != classB)
                        indicesToCheck.add(n);
                }
                //所有样本在所有属性上取值相同，无法划分，返回样本数最多的类;
                if (indicesToCheck.size() == 0)
                    sameClass++;
            }
            if(sameClass == parent.attrArr.size()){
                parent.isLeaf = true;
                parent.Class = GetMajorityClass(parent.data);
                return;
            }

            for (int m:parent.attrArr){
                SortAtAttribute(parent.data, m);//sorts on a particular column in the row
                ArrayList<Integer> indicesToCheck = new ArrayList<Integer>();
                for (int n=1; n<Nsub; n++){
                    int classA = GetClass(parent.data.get(n-1));
                    int classB = GetClass(parent.data.get(n));
                    if (classA != classB)
                        indicesToCheck.add(n);
                }
//				System.out.print("indices to check for tree : "+Ntreenum);
//				for (int n:indicesToCheck)
//					System.out.print(" "+n);
//				System.out.print("\n ");
                if (indicesToCheck.size() > MIN_SIZE_TO_CHECK_EACH){
                    for (int i=0; i<indicesToCheck.size(); i+=INDEX_SKIP){
                        //System.out.println("Checking positions for index : "+i+" and tree :"+Ntreenum);
                        CheckPosition(m, indicesToCheck.get(i), Nsub, lowestE, parent, Ntreenum);
                        if (lowestE.d == 0)//lowestE now has the minimum conditional entropy so IG is max there
                            break;
                    }
                }
                else {
                    for (int index:indicesToCheck){
                        CheckPosition(m, index, Nsub, lowestE, parent, Ntreenum);
                        if (lowestE.d == 0)
                            break;
                    }
                }
//				BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
//				System.out.println("************************* lowest e:"+lowestE.d);
//				try {reader.readLine();} catch (IOException e){}
                if (lowestE.d == 0)
                    break;
            }
            //从属性集合删除分裂属性
            Iterator<Integer> it = parent.attrArr.iterator();
            while(it.hasNext()){
                int attr = it.next();
                if (attr == parent.splitAttributeM){
                    it.remove();
                }
            }
            parent.left.attrArr = parent.attrArr;
            parent.right.attrArr = parent.attrArr;
//			System.out.print("\n");
//			System.out.print("split attrubute num:"+parent.splitAttributeM+" at val:"+parent.splitValue+" n:"+parent.data.size()+" ");
//			PrintOutClasses(parent.data);
            //			System.out.println("\nmadeSplit . . .");
//			PrintOutNode(parent," ");
//			PrintOutNode(parent.left,"    ");
//			PrintOutNode(parent.right,"    ");

            //-------------------------------Step D
            //------------Left Child
            if (parent.left.data.size() == 1){//训练集为空
                parent.left.isLeaf = true;
                parent.left.Class = GetClass(parent.left.data.get(0));
            }
            else if (parent.left.attrArr.size() == 0){//属性集为空
                parent.left.isLeaf = true;
                parent.Class = GetMajorityClass(parent.left.data);
            }
//            else if (parent.left.data.size() < MIN_NODE_SIZE){
//                parent.left.isLeaf = true;
//                parent.left.Class = GetMajorityClass(parent.left.data);
//            }
            else {
                Class = CheckIfLeaf(parent.left.data);
                if (Class == null){
                    parent.left.isLeaf = false;
                    parent.left.Class = null;
//					System.out.println("make branch left: m:"+m);
                }
                else {//训练集样本全属于同一类别
                    parent.left.isLeaf = true;
                    parent.left.Class = Class;
                }
            }
            //------------Right Child
            if (parent.right.data.size() == 1){//训练集为空
                parent.right.isLeaf = true;
                parent.right.Class = GetClass(parent.right.data.get(0));
            }
            else if (parent.right.attrArr.size() == 0){//属性集为空
                parent.right.isLeaf = true;
                parent.Class = GetMajorityClass(parent.right.data);
            }
//            else if (parent.left.data.size() < MIN_NODE_SIZE){
//                parent.left.isLeaf = true;
//                parent.left.Class = GetMajorityClass(parent.left.data);
//            }
            else {
                Class = CheckIfLeaf(parent.right.data);
                if (Class == null){
                    parent.right.isLeaf = false;
                    parent.right.Class = null;
//					System.out.println("make branch right: m:"+m);
                }
                else {//训练集样本全属于同一类别
                    parent.right.isLeaf = true;
                    parent.right.Class = Class;
                }
            }


            if (!parent.left.isLeaf){
                RecursiveSplit(parent.left, Ntreenum);
            }

//			else {
//				System.out.print("left leaf! Class:"+parent.left.Class+"  ");
//				PrintOutClasses(parent.left.data);
//			}
            if (!parent.right.isLeaf)
                RecursiveSplit(parent.right, Ntreenum);
//			else {
//				System.out.print("leaf right! Class:"+parent.right.Class+"  ");
//				PrintOutClasses(parent.right.data);
//			}
        }
    }
    /**
     * Given a data matrix, return the most popular Y value (the class)
     * @param data	The data matrix
     * @return		The most popular class
     */
    private int GetMajorityClass(List<float[]> data){
        int[] counts=new int[this.forest.C];
        for (float[] record:data){
            int Class = (int) record[record.length-1];//GetClass(record);
            counts[Class-1]++;
        }
        int index=-99;
        int max=Integer.MIN_VALUE;
        for (int i=0;i<counts.length;i++){
            if (counts[i] > max){
                max=counts[i];
                index=i+1;
            }
        }
        return index;
    }

    /**
     * Checks the {@link #CalcEntropy(double[]) entropy} of an index in a data matrix at a particular attribute (m)
     * and returns the entropy. If the entropy is lower than the minimum to date (lowestE), it is set to the minimum.
     *
     * The total entropy is calculated by getting the sub-entropy for below the split point and upper the split point.
     * The sub-entropy is calculated by first getting the {@link #GetClassProbs(List) proportion} of each of the classes
     * in this sub-data matrix. Then the entropy is {@link #CalcEntropy(double[]) calculated}. The lower sub-entropy
     * and upper sub-entropy are then weight averaged to obtain the total entropy.
     *
     * @param m				the attribute to split on
     * @param n				the index to check(rowID)
     * @param Nsub			the num of records in the data matrix
     * @param lowestE		the minimum entropy to date
     * @param parent		the parent node
     * @return				the entropy of this split
     */
    private double CheckPosition(int m, int n, int Nsub, DoubleWrap lowestE, TreeNode parent, int nTre){
        //                       var,	index,	train.size,	lowest number,	for a tree
        //System.out.println("Checking position of the index attribute of tree :"+nTre);
        if (n < 1) //exit conditions
            return 0;
        if (n > Nsub)
            return 0;

        List<float[]> lower = GetLower(parent.data, n);
        List<float[]> upper = GetUpper(parent.data, n);
        if (lower == null)
            System.out.println("lower list null");
        if (upper == null)
            System.out.println("upper list null");
        double[] p = GetClassProbs(parent.data);
        double[] pl = GetClassProbs(lower);
        double[] pu = GetClassProbs(upper);
        double eP = CalcEntropy(p);
        double eL = CalcEntropy(pl);
        double eU = CalcEntropy(pu);

        double e = eP - eL * lower.size()/(double)Nsub - eU * upper.size()/(double)Nsub;
//		System.out.println("g:"+parent.generation+" N:"+parent.data.size()+" M:"+RandomForest.M+" Ms:"+RandomForest.Ms+" n:"+n+" m:"+m+" val:"+parent.data.get(n)[m]+"                                                           e:"+e);
//		out.write(m+","+n+","+parent.data.get(n)[m]+","+e+"\n");
        if (e > lowestE.d){
            lowestE.d = e;
//			System.out.print("-");
            parent.splitAttributeM = m;
            parent.splitValue = parent.data.get(n)[m];
            parent.left.data = lower;
            parent.right.data = upper;

        }
        return e;//entropy
    }
    /**
     * Given a data record, return the Y value - take the last index
     *
     * @param record		the data record
     * @return				its y value (class)
     */
    public int GetClass(float[] record){
        return (int) record[this.forest.M];
    }
    /**
     * Given a data matrix, check if all the y values are the same. If so,
     * return that y value, null if not
     *
     * @param data		the data matrix
     * @return			the common class (null if not common)
     */
    private Integer CheckIfLeaf(List<float[]> data){
//		System.out.println("checkIfLeaf");
        boolean isLeaf = true;
        int ClassA = GetClass(data.get(0));
        for (int i=1; i<data.size(); i++){
            float[] recordB = data.get(i);
            if (ClassA != GetClass(recordB)){
                isLeaf = false;
                break;
            }
        }
        if (isLeaf)
            return GetClass(data.get(0));
        else
            return null;
    }
    /**
     * Split a data matrix and return the upper portion
     *
     * @param data		the data matrix to be split
     * @param nSplit	index in a sub-data matrix that we will return all data records above it
     * @return			the upper sub-data matrix
     */
    private List<float[]> GetUpper(List<float[]> data, int nSplit){
        int N =  data.size();
        List<float[]> upper = new ArrayList<>(N-nSplit);
        for (int n=nSplit; n<N; n++)
            upper.add(data.get(n));
        return upper;
    }
    /**
     * Split a data matrix and return the lower portion
     *
     * @param data		the data matrix to be split
     * @param nSplit	this index in a sub-data matrix that return all data records below it
     * @return			the lower sub-data matrix
     */
    private List<float[]> GetLower(List<float[]> data, int nSplit){
        List<float[]> lower = new ArrayList<>(nSplit);
        for (int n=0; n<nSplit; n++)
            lower.add(data.get(n));
        return lower;
    }
    /**
     * This class compares two data records by numerically comparing a specified attribute
     *
     * @author kapelner
     *
     */
    private class AttributeComparator implements Comparator {
        /** the specified attribute */
        private int m;
        /**
         * Create a new comparator
         * @param m		the attribute in which to compare on
         */
        public AttributeComparator(int m){
            this.m = m;
        }
        /**
         * Compare the two data records. They must be of type int[].
         *
         * @param o1		data record A
         * @param o2		data record B
         * @return			-1 if A[m] < B[m], 1 if A[m] > B[m], 0 if equal
         */
        public int compare(Object o1, Object o2){
            float a = ((float[])o1)[m];
            float b = ((float[])o2)[m];
            int result = Float.compare(a,b);
            if (result > 0)
                return 1;
            else if (result < 0)
                return -1;
            else
                return 0;
//            if (a < b)
//                return -1;
//            if (a > b)
//                return 1;
//            else
//                return 0;
        }
    }
    /**
     * Sorts a data matrix by an attribute from lowest record to highest record
     * @param data			the data matrix to be sorted
     * @param m				the attribute to sort on
     */
    @SuppressWarnings("unchecked")
    private void SortAtAttribute(List<float[]> data, int m){
        Collections.sort(data, new AttributeComparator(m));
    }
    /**
     * Given a data matrix, return a probability mass function representing
     * the frequencies of a class in the matrix (the y values)
     *
     * @param records		the data matrix to be examined
     * @return				the probability mass function
     */
    private double[] GetClassProbs(List<float[]> records){

        double N = records.size();

        int[] counts = new int[this.forest.C];//the num of target class
//		System.out.println("counts:");
//		for (int i:counts)
//			System.out.println(i+" ");

        for (float[] record:records)
            counts[GetClass(record)-1]++;

        double[] ps = new double[this.forest.C];
        for (int j=0; j<this.forest.C; j++)
            ps[j] = counts[j]/N;
//		System.out.print("probs:");
//		for (double p:ps)
//			System.out.print(" "+p);
//		System.out.print("\n");
        return ps;
    }
    /** ln(2) */
    private static final double logoftwo = Math.log(2);
    /**
     * Given a probability mass function indicating the frequencies of
     * class representation, calculate an "entropy" value using the method
     * in Tan Steinbach Kumar's "Data Mining" textbook
     *
     * @param ps			the probability mass function
     * @return				the entropy value calculated
     */
    private double CalcEntropy(double[] ps){
        double e = 0;
        for (double p:ps){
            if (p != 0) //otherwise it will divide by zero - see TSK p159
                e += p * Math.log(p)/Math.log(2);
        }
        return -e; //according to TSK p158
    }
    /**
     * Of the M attributes, select {@link RandomForest#Ms Ms} at random.
     *
     * @return		The list of the Ms attributes' indices
     */
    private ArrayList<Integer> GetVarsToInclude() {
        boolean[] whichVarsToInclude = new boolean[this.forest.M];

        for (int i=0; i<this.forest.M; i++)//初始化全为false
            whichVarsToInclude[i]=false;

        while (true){
            int a = (int)Math.floor(Math.random() * this.forest.M);//左闭右开 [0，1)
            whichVarsToInclude[a] = true;
            int N = 0;
            for (int i=0; i<this.forest.M; i++)
                if (whichVarsToInclude[i])
                    N++;
            if (N == this.forest.Ms)
                break;
        }

        ArrayList<Integer> shortRecord = new ArrayList<Integer>(this.forest.Ms);

        for (int i=0; i<this.forest.M; i++)
            if (whichVarsToInclude[i])
                shortRecord.add(i);
        return shortRecord;
    }

    /**
     * Create a boostrap sample of a data matrix
     * @param data		the data matrix to be sampled
     * @param train		the bootstrap sample
     * @param val		the records that are absent in the bootstrap sample
     * @param numb      the tree number
     */
    private void BootStrapSample(ArrayList<float[]> data, ArrayList<float[]> train, ArrayList<float[]> val, int numb){
        ArrayList<Integer> indices = new ArrayList<Integer>(N);
        for (int n=0; n<N; n++)
            indices.add((int)Math.floor(Math.random() * N));
        ArrayList<Boolean> IsIn = new ArrayList<Boolean>(N);
        for (int n=0; n<N; n++)
            IsIn.add(false); //initialize it first
        for (int index:indices){
            train.add((data.get(index)).clone());//train has duplicated  record
            IsIn.set(index, true);
        }
        for (int i=0; i<N; i++)
            if (!IsIn.get(i))
                val.add((data.get(i)).clone());
        //System.out.println("created testing-data for tree : "+numb);//everywhere its set to false we get those to test data

//		System.out.println("bootstrap N:"+N+" size of bootstrap:"+bootstrap.size());
    }
    /**
     * Recursively deletes all data records from the tree. This is run after the tree
     * has been computed and can stand alone to classify incoming data.
     *
     * @param node		initially, the root node of the tree
     */
    private void FlushData(TreeNode node){
        node.data = null;
        if (node.left != null)
            FlushData(node.left);
        if (node.right != null)
            FlushData(node.right);
    }

//	// possible to clone trees
//	private DecisionTree(){}
//	public DecisionTree clone(){
//		DecisionTree copy=new DecisionTree();
//		copy.root=root.clone();
//		return copy;
//	}

    /**
     * Get the number of data records in the test data matrix that were classified correctly
     */
    public int getNumCorrect(){
        return correct;
    }
    /**
     * Get the number of data records left out of the bootstrap sample
     */
    public int getTotalNumInTestSet(){
        return testN;
    }
    /**
     * Get the importance level of attribute m for this tree
     */
    public int getImportanceLevel(int m){
        return importance[m];
    }
//	private void PrintOutNode(TreeNode parent,String init){
//		try {
//			System.out.println(init+"node: left"+parent.left.toString());
//		} catch (Exception e){
//			System.out.println(init+"node: left null");
//		}
//		try {
//			System.out.println(init+" right:"+parent.right.toString());
//		} catch (Exception e){
//			System.out.println(init+"node: right null");
//		}
//		try {
//			System.out.println(init+" isleaf:"+parent.isLeaf);
//		} catch (Exception e){}
//		try {
//			System.out.println(init+" splitAtrr:"+parent.splitAttributeM);
//		} catch (Exception e){}
//		try {
//			System.out.println(init+" splitval:"+parent.splitValue);
//		} catch (Exception e){}
//		try {
//			System.out.println(init+" class:"+parent.Class);
//		} catch (Exception e){}
//		try {
//			System.out.println(init+" data size:"+parent.data.size());
//			PrintOutClasses(parent.data);
//		} catch (Exception e){
//			System.out.println(init+" data: null");
//		}
//	}
//	private void PrintOutClasses(List<int[]> data){
//		try {
//			System.out.print(" (n="+data.size()+") ");
//			for (int[] record:data)
//				System.out.print(GetClass(record));
//			System.out.print("\n");
//		}
//		catch (Exception e){
//			System.out.println("PrintOutClasses: data null");
//		}
//	}
//	public static void PrintBoolArray(boolean[] b) {
//		System.out.print("vars to include: ");
//		for (int i=0;i<b.length;i++)
//			if (b[i])
//				System.out.print(i+" ");
//		System.out.print("\n\n");
//	}
//
//	public static void PrintIntArray(List<int[]> lower) {
//		System.out.println("tree");
//		for (int i=0;i<lower.size();i++){
//			int[] record=lower.get(i);
//			for (int j=0;j<record.length;j++){
//				System.out.print(record[j]+" ");
//			}
//			System.out.print("\n");
//		}
//		System.out.print("\n");
//		System.out.print("\n");
//	}
}
