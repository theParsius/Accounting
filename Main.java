import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        int numCommands = input.nextInt();
        String[] commands = new String[numCommands];

//        getting all the commands
        input.nextLine();
        for (int i = 0; i < commands.length; i++) {
            commands[i] = input.nextLine();
        }

        HashTable hashTable = new HashTable();

        for (int i = 0; i < commands.length; i++) {
            if (commands[i].charAt(0) == '1') {
                String[] command = commands[i].split(" ", -1);
                hashTable.insert(command[1]);
                hashTable.insert(command[2]);
            }
        }


        AVLTree[] avlTrees = new AVLTree[hashTable.size];
        for (int i = 0; i < avlTrees.length; i++) {
            avlTrees[i] = new AVLTree();
        }

        SegmentNode[] segmentMaxNodes = new SegmentNode[hashTable.size];
        SegmentNode[] segmentMinNodes = new SegmentNode[hashTable.size];
        for(int i=0 ; i<segmentMaxNodes.length ; i++) {
            segmentMaxNodes[i] = new SegmentNode(hashTable.get(i), 0);
            segmentMinNodes[i] = new SegmentNode(hashTable.get(i), 0);
        }

        SegmentTree maxSegmentTree = new SegmentTree(segmentMaxNodes);
        maxSegmentTree.buildMaxTree(segmentMaxNodes, 0,0,segmentMaxNodes.length - 1);
        SegmentTree minSegmentTree = new SegmentTree(segmentMinNodes);
        minSegmentTree.buildMinTree(segmentMinNodes, 0, 0, segmentMinNodes.length - 1);


//        processing the commands
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].charAt(0) == '1') {
                String[] command = commands[i].split(" ", -1);
                long value = Math.round(Double.parseDouble(command[3]) * 100);
                int indexFirstName = hashTable.search(command[1]);
                int indexSecondName = hashTable.search(command[2]);
                avlTrees[indexFirstName].insert(indexSecondName, value);
                avlTrees[indexSecondName].insert(indexFirstName, -value);
                maxSegmentTree.updateMaxTree(0,0,segmentMaxNodes.length - 1, indexFirstName, value);
                maxSegmentTree.updateMaxTree(0, 0, segmentMaxNodes.length - 1, indexSecondName, -value);
                minSegmentTree.updateMinTree(0,0,segmentMinNodes.length - 1, indexFirstName, value);
                minSegmentTree.updateMinTree(0, 0, segmentMinNodes.length - 1, indexSecondName, -value);
            } else if (commands[i].charAt(0) == '2') {
                SegmentNode node = minSegmentTree.queryMinTree(0,0,segmentMinNodes.length -1 ,0,segmentMinNodes.length -1);
                if(node.value == 0) {
                    System.out.println(-1);
                } else {
                    System.out.println(node.toString());
                }

            } else if (commands[i].charAt(0) == '3') {
                SegmentNode node = maxSegmentTree.queryMaxTree(0,0,segmentMaxNodes.length - 1,0,segmentMaxNodes.length -1);
                if(node.value == 0) {
                    System.out.println(-1);
                } else {
                    System.out.println(node.toString());
                }

            } else if (commands[i].charAt(0) == '4') {
                String[] command = commands[i].split(" ", -1);
                int indexName = hashTable.search(command[1]);
                AVLTree avlTree = avlTrees[indexName];
                System.out.println(avlTree.numDebt);

            } else if (commands[i].charAt(0) == '5') {
                String[] command = commands[i].split(" ", -1);
                int indexName = hashTable.search(command[1]);
                AVLTree avlTree = avlTrees[indexName];
                System.out.println(avlTree.numClaimant);

            } else if (commands[i].charAt(0) == '6') {
                String[] command = commands[i].split(" ", -1);
                int indexFirstName = hashTable.search(command[1]);
                int indexSecondName = hashTable.search(command[2]);
                AVLTree avlTree = avlTrees[indexFirstName];
                AVLNode avlNode = avlTree.search(avlTree.root, indexSecondName);
                if(avlNode != null) {
                    double value = ((double) (avlNode.value)) / 100;
                    if (value == 0) {
                        System.out.printf("%.2f" ,value);
                        System.out.println();
                    } else {
                        System.out.printf("%.2f" ,-value);
                        System.out.println();
                    }
                } else {
                    System.out.printf( "%.2f", 0.0);
                    System.out.println();
                }
            }
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------

// hash table
class HashTable {
    HashTableNode[] table;
    private int MOD = 10000000;
    String[] names;
    int size = 0;

    public HashTable() {
        table = new HashTableNode[MOD];
        names = new String[MOD];
    }

    public void insert(String name) {
        int place = hashFunction(name);
        HashTableNode node = new HashTableNode(name);
        if(table[place] == null) {
            node.index = size;
            if(names[node.index] == null) {
                names[node.index] = name;
            }
            table[place] = node;
            size++;
        } else {
            HashTableNode currentNode = table[place];
            node.index = size;
            node.nextNode = currentNode.nextNode;
            currentNode.nextNode = node;
            names[node.index] = name;
            size++;
        }
    }

    public int search(String name) {
        int place = hashFunction(name);
        if(table[place] == null) {
            return -1;
        } else {
            HashTableNode currentNode = table[place];
            while(!(currentNode.name.equals(name)) && currentNode.nextNode != null) {
                currentNode = currentNode.nextNode;
            }
            return currentNode.index;
        }
    }

    public String get(int index) {
        return names[index];
    }

    int MAX_SIZE = 10000000;
    long BASE = 27;

    private int hashFunction(String name) {
        long place = 0;
        for (int i = 0; i < name.length(); i++) {
            place = place * BASE + (long)name.charAt(i) - (long)'a' + 1;
            if (place >= MAX_SIZE) {
                place %= MAX_SIZE;
            }
        }
        return (int)Math.abs(place);
    }

}

// hashtable node
class HashTableNode {
    HashTableNode nextNode;
    String name;
    int index;

    public HashTableNode(String name) {
        this.name = name;
    }
}

// avl tree
class AVLTree {
    public AVLNode root;
    int numDebt = 0;
    int numClaimant = 0;

    public AVLTree() {
        root = null;
    }

    public int getBalance(AVLNode node) {
        if(node == null) {
            return 0;
        } else {
            return height(node.leftChild) - height(node.rightChild);
        }
    }

    public AVLNode insert(int key, long value) {
        if(root == null) {
            root = new AVLNode(key, value);
            if(value > 0) {
                numClaimant++;
            } else if(value < 0) {
                numDebt++;
            }
            return root;
        } else {
            return insert(root, key, value);
        }
    }

    public AVLNode insert(AVLNode node, int key, long value) {
        if (node == null) {
            if(value > 0) {
                numClaimant++;
            } else if(value < 0) {
                numDebt++;
            }
            return (new AVLNode(key, value));
        }
        if (key < node.key) {
            node.leftChild = insert(node.leftChild, key, value);
        } else if (key > node.key) {
            node.rightChild = insert(node.rightChild, key, value);
        } else if(key == node.key) {
            if(node.value > 0 && (value + node.value == 0)) {
                numClaimant--;
                node.value += value;
                return node;
            } else if(node.value < 0 && (value + node.value == 0)) {
                numDebt--;
                node.value += value;
                return node;
            } else if(node.value > 0 && (value + node.value > 0)) {
                node.value += value;
                return node;
            } else if(node.value > 0 && (value + node.value) < 0) {
                numDebt++;
                numClaimant--;
                node.value += value;
                return node;
            } else if(node.value < 0 && (value + node.value > 0)) {
                numDebt--;
                numClaimant++;
                node.value += value;
                return node;
            } else if(node.value < 0 && (value + node.value < 0)) {
                node.value += value;
                return node;
            } else if(node.value == 0 && (value + node.value > 0)) {
                numClaimant++;
                node.value += value;
                return node;
            } else if(node.value == 0 && (value + node.value < 0)) {
                numDebt++;
                node.value += value;
                return node;
            }
        }

        node.height = max(height(node.leftChild), height(node.rightChild)) + 1;

//        if (getBalance(node) > 1 && key < node.leftChild.key) {
//            return rightRotate(node);
//        }
//
//        if (getBalance(node) > 1 && key > node.leftChild.key) {
//            node.leftChild = leftRotate(node.leftChild);
//            return rightRotate(node);
//        }
//
//        if (getBalance(node) < -1 && key > node.rightChild.key) {
//            return leftRotate(node);
//        }
//
//        if (getBalance(node) < -1 && key < node.rightChild.key) {
//            node.rightChild = rightRotate(node.rightChild);
//            return leftRotate(node);
//        }

        return node;
    }

//    public AVLNode rightRotate(AVLNode node) {
//        if(node.leftChild != null) {
//            AVLNode newParent = node.leftChild;
//            AVLNode temp = newParent.rightChild;
//
//            newParent.rightChild = node;
//            node.leftChild = temp;
//
//            node.height = max(height(node.leftChild), height(node.rightChild)) + 1;
//            newParent.height = max(height(newParent.leftChild), height(newParent.rightChild)) + 1;
//
//            return newParent;
//        } else {
//            return node;
//        }
//    }

//    public AVLNode leftRotate(AVLNode node) {
//        if(node.rightChild != null) {
//            AVLNode newParent = node.rightChild;
//            AVLNode temp = newParent.leftChild;
//
//            newParent.leftChild = node;
//            node.rightChild = temp;
//
//            node.height = max(height(node.leftChild), height(node.rightChild));
//            newParent.height = max(height(newParent.leftChild), height(newParent.rightChild));
//
//            return newParent;
//        } else {
//            return node;
//        }
//    }

    public void preOrderPrint(AVLNode root) {
        if(root != null) {
            System.out.print(root.value / 100);
            System.out.print(" ");
            preOrderPrint(root.leftChild);
            preOrderPrint(root.rightChild);
        }
    }

    public void inOrderPrint(AVLNode root) {
        if(root != null) {
            inOrderPrint(root.leftChild);
            System.out.print(root.value / 100);
            System.out.print(" ");
            inOrderPrint(root.rightChild);
        }
    }

    public void postOrderPrint(AVLNode root) {
        if(root != null) {
            postOrderPrint(root.leftChild);
            postOrderPrint(root.rightChild);
            System.out.print(root.value / 100);
            System.out.print(" ");
        }
    }

    public AVLNode minValueNode(AVLNode root) {
        AVLNode current = root;
        while(current.leftChild != null) {
            current = current.leftChild;
        }
        return current;
    }

    public AVLNode maxValueNode(AVLNode root) {
        AVLNode current = root;
        while(current.rightChild != null) {
            current = current.rightChild;
        }
        return current;
    }

//    public AVLNode deleteNode(AVLNode root, int key, double value) {
//        if (root == null) {
//            return root;
//        }
//
//        if (key < root.key) {
//            root.leftChild = deleteNode(root.leftChild, key, value);
//        } else if (key > root.key) {
//            root.rightChild = deleteNode(root.rightChild, key, value);
//        } else {
//            if ((root.leftChild == null) || (root.rightChild == null)) {
//                AVLNode temp;
//                if (root.leftChild != null) {
//                    temp = root.leftChild;
//                } else {
//                    temp = root.rightChild;
//                }
//
//                if (temp == null) {
//                    temp = root;
//                    root = null;
//                } else {
//                    root = temp;
//                }
//
//                temp = null;
//            } else {
//                AVLNode temp = minValueNode(root.rightChild);
//
//                root.value = temp.value;
//
//                root.rightChild = deleteNode(root.rightChild, key, temp.value);
//            }
//        }
//        if (root == null) {
//            return root;
//        }
//
//        root.height = Math.max(height(root.leftChild), height(root.rightChild)) + 1;
//
//        int balance = getBalance(root);
//
//        if (getBalance(root) > 1 && getBalance(root.leftChild) >= 0) {
//            return rightRotate(root);
//        }
//
//        if (getBalance(root) > 1 && getBalance(root.leftChild) < 0) {
//            root.leftChild =  leftRotate(root.leftChild);
//            return rightRotate(root);
//        }
//
//        if (balance < -1 && getBalance(root.rightChild) <= 0) {
//            return leftRotate(root);
//        }
//
//        if (balance < -1 && getBalance(root.rightChild) > 0) {
//            root.rightChild = rightRotate(root.rightChild);
//            return leftRotate(root);
//        }
//
//        return root;
//    }

    public AVLNode search(AVLNode root, int key) {
        if(root == null) {
            return null;
        }

        if(key > root.key) {
            return search(root.rightChild, key);
        } else if(key < root.key) {
            return search(root.leftChild, key);
        } else {

            return root;
        }
    }

    public int height(AVLNode node) {
        if (node == null) {
            return 0;
        }
        return node.height;
    }

    public int max(int num1, int num2) {
        if(num1 > num2) {
            return num1;
        } else {
            return num2;
        }
    }
}

// avl node
class AVLNode {
    int key;
    long value;
    int height;
    String name;
    AVLNode rightChild;
    AVLNode leftChild;

    public AVLNode(long value) {
        this.value = value;
        rightChild = null;
        leftChild = null;
    }

    public AVLNode(int key, long value) {
        this.key = key;
        this.value = value;
    }

    public AVLNode(String name) {
        this.name = name;
    }
}

// segment tree
class SegmentTree {
    private SegmentNode[] maxTree;
    private SegmentNode[] minTree;

    private long positiveInfinity = (long) Double.POSITIVE_INFINITY;
    private long negativeInfinity = (long) Double.NEGATIVE_INFINITY;

    private SegmentNode positiveInfinityNode = new SegmentNode("zzzzzzzzz", positiveInfinity);
    private SegmentNode negativeInfinityNode = new SegmentNode(" ", negativeInfinity);

    public SegmentTree(SegmentNode[] array) {
        int temp = (int) (Math.ceil(Math.log(array.length) / Math.log(2)));
        int maxSize = 2 * (int) Math.pow(2, temp) - 1;
        maxTree = new SegmentNode[maxSize];
        minTree = new SegmentNode[maxSize];
    }

    public void buildMaxTree(SegmentNode[] array, int node, int start, int end) {
        if(start == end) {
            maxTree[node] = array[start];
        } else {
            int mid = (start + end) / 2;
            buildMaxTree(array, (2 * node) + 1, start, mid);
            buildMaxTree(array, (2 * node) + 2, mid + 1, end);
            maxTree[node] = max(maxTree[2 * node + 1], maxTree[2 * node + 2]);
        }
    }

    public void buildMinTree(SegmentNode[] array, int node, int start, int end) {
        if(start == end) {
            minTree[node] = array[start];
        } else {
            int mid = (start + end) / 2;
            buildMinTree(array, (2 * node) + 1, start, mid);
            buildMinTree(array, (2 * node) + 2, mid + 1, end);
            minTree[node] = min(minTree[2 * node + 1], minTree[2 * node + 2]);
        }
    }

    public void updateMaxTree(int node, int start, int end, int index, long value) {
        if(!(start <= index && index <= end)) {
            return;
        }
        if(start == end) {
            maxTree[node].value += value;
        } else {
            int mid = (start + end) / 2;
            if(start <= index && index <= mid) {
                updateMaxTree((2 * node) + 1, start, mid, index, value);
            }
            if(mid + 1 <= index && index <= end){
                updateMaxTree((2 * node) + 2, mid + 1, end, index, value);
            }
            maxTree[node] = max(maxTree[(2 * node) + 1], maxTree[(2 * node) + 2]);
        }
    }

    public void updateMinTree(int node, int start, int end, int index, long value) {
        if(!(start <= index && index <= end)) {
            return;
        }
        if(start == end) {
            minTree[node].value += value;
        } else {
            int mid = (start + end) / 2;
            if(start <= index && index <= mid) {
                updateMinTree((2 * node) + 1, start, mid, index, value);
            } else {
                updateMinTree((2 * node) + 2, mid + 1, end, index, value);
            }
            minTree[node] = min(minTree[(2 * node) + 1], minTree[(2 * node) + 2]);
        }
    }

    public SegmentNode queryMaxTree(int node, int start, int end, int lowerBound, int higherBound) {
        if(higherBound < start || end < lowerBound) {
            return negativeInfinityNode;
        }
        if(lowerBound <= start && end <= higherBound) {
            return maxTree[node];
        }
        int mid = (start + end) / 2;
        SegmentNode firstHalf = queryMaxTree((2 * node) + 1, start, mid, lowerBound, higherBound);
        SegmentNode secondHalf = queryMaxTree((2 * node) + 2, mid + 1, end, lowerBound, higherBound);
        return max(firstHalf, secondHalf);
    }

    public SegmentNode queryMinTree(int node, int start, int end, int lowerBound, int higherBound) {
        if(higherBound < start || end < lowerBound) {
            return positiveInfinityNode;
        }
        if(lowerBound <= start && end <= higherBound) {
            return minTree[node];
        }
        int mid = (start + end) / 2;
        SegmentNode firstHalf = queryMinTree((2 * node) + 1, start, mid, lowerBound, higherBound);
        SegmentNode secondHalf = queryMinTree((2 * node) + 2, mid + 1, end, lowerBound, higherBound);
        return min(firstHalf, secondHalf);
    }

    public SegmentNode max(SegmentNode node1, SegmentNode node2) {
        if(node1.compareTo(node2) > 0) {
            return node1;
        } else if(node1.compareTo(node2) < 0) {
            return node2;
        } else {
            if(node1.name.compareTo(node2.name) > 0) {
                return node2;
            } else {
                return node1;
            }
        }
    }

    public SegmentNode min(SegmentNode node1, SegmentNode node2) {
        if(node1.compareTo(node2) < 0) {
            return node1;
        } else if(node1.compareTo(node2) > 0) {
            return node2;
        } else {
            if(node1.name.compareTo(node2.name) > 0) {
                return node2;
            } else {
                return node1;
            }
        }
    }

    public void printMaxTree() {
        for(int i=0 ; i<maxTree.length ; i++) {
            System.out.print(maxTree[i] + " ");
        }
        System.out.println();
    }

    public void printMinTree() {
        for(int i=0 ; i<minTree.length ; i++) {
            System.out.print(minTree[i] + " ");
        }
        System.out.println();
    }

}

// segment node
class SegmentNode implements Comparable {
    String name;
    long value;

    public SegmentNode(String name, long value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return name;
    }

    //    @Override
//    public int compareTo(Object o) {
//        SegmentNode segmentNode = (SegmentNode) o;
//        if(this.value > segmentNode.value) {
//            return 1;
//        } else if(this.value < segmentNode.value) {
//            return -1;
//        }
//        return -(this.name.compareTo(segmentNode.name));
//    }
    @Override
    public int compareTo(Object o) {
        SegmentNode segmentNode = (SegmentNode) o;
        if(this.value > segmentNode.value) {
            return 1;
        } else if(this.value < segmentNode.value) {
            return -1;
        } else {
            return 0;
        }
    }
}

// trie tree
class TrieTree {
    TrieNode root;

    public void insert(String name) {
        int level = 0;
        int index;

        TrieNode currentNode = root;

        while(level != name.length()) {
            index = name.charAt(level) - 'a';
            if(currentNode.children[index] == null) {
                currentNode.children[index] = new TrieNode();
            }
            currentNode = currentNode.children[index];
            level++;
        }
        currentNode.endOfWord = true;
    }

    public boolean search(String name) {
        int level = 0;
        int index;

        TrieNode currentNode = root;

        while(level != name.length()) {
            index = name.charAt(level) - 'a';
            if(currentNode.children[index] == null) {
                return false;
            }
            currentNode = currentNode.children[index];
        }
        return (currentNode != null && currentNode.endOfWord);
    }
}

// trie node
class TrieNode{
    int sizeAlphabet = 26;
    int index;
    TrieNode[] children = new TrieNode[sizeAlphabet];
    boolean endOfWord;

    TrieNode() {
        endOfWord = false;
        for(int i=0 ; i<children.length ; i++) {
            children[i] = null;
        }
    }
}