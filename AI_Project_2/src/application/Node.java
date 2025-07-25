package application;

import java.util.*;

public class Node {
    public String feature;                       
    public Map<String, Node> branches = new HashMap<>();    
    public String label;                        

    public boolean isLeaf() {
        return label != null;
    }
}
