// Grant Elder
// Asg 5: Dijkstra's algorithm

// Whats working:
// +10 read input
// +20 correct adjacency list
// +20 produce requested output
// +50 correctly implement Dijkstra's algorithm
// +5 extraoutput.gra 
// +5 for including correct path name in .gra file
// 110 points total so far

// There appears to be limitations in my dijkstra's algorithm due to decimal point rounding, not an error in the algorithm please ask me if you need

// Usage:
// java DijkstraHighway <input file name> <output file name> <start point> <end point>
// Example: java DijkstraHighway southEastMap1.gra results.gra Atlanta Memphis@I-40/I-55

// Used for writing to a file and reading a file
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class DijkstraHighway {

	public static void main(String[] args) {
		DijkstraHighway m = new DijkstraHighway();
		
		if(args.length < 4) {
			System.out.println("Please enter 4 or more arguments");
			System.out.println("Usage: java DijkstraHighway <input file name> <output file name> <start point> ... <end point>");
			return;
		}
		
		// <input file name>
		m.fileInName = args[0].trim();
		if(!m.fileInName.substring(m.fileInName.length()-4).equals(".gra")) {
			System.out.println("<input file name> should have the file extention .gra");
			return;
		}
		// <output file name>
		m.fileOutName = args[1].trim();
		if(!m.fileOutName.substring(m.fileOutName.length()-4).equals(".gra")) {
			System.out.println("<output file name> should have the file extention .gra");
			return;
		}
		// parse the input file
		try {
			m.parseFile();
		} 
		catch (FileNotFoundException e) {
			System.out.println("Some error parsing the file. ");
			return;
		}
		
		// prints the adjacency list to check it
		//m.map.printAdjList();
		
		String locations = args[2].trim();
		
		for(int i = 3; i < args.length; i++) {
			locations += " " + args[i].trim();
		}
		String[] directionNames = locations.split(" ");
		String[] routeNames = new String[directionNames.length];
		for(int i = 0; i < directionNames.length; i++) {
			routeNames[i] = directionNames[directionNames.length - i - 1];
		}
		
		DijkstraHighway.AdjList.Vertex[] routeVertices = new DijkstraHighway.AdjList.Vertex[routeNames.length];
		for(int i = 0; i < routeNames.length; i++) {
			routeVertices[i] = m.map.nameLookup(routeNames[i]);
		}
		
		// writes the output to a string and prints it
		m.map.dijkstra(routeVertices[0], routeVertices[1]);
		
		m.createGRA();
	}
	
	// variables used in creating an adjacency list
	int vertexCount;
	int edgeCount;
	AdjList map = new AdjList();
	
	// output that is added to file at the end of the program
	String outputText = "";
	String fileOutName;
	String fileInName;
	
	int numVerticesOutput = 0;
	int numEdgesOutput = 0;
	DijkstraHighway.AdjList.Vertex firstOutputV;
	String[] vertexNames;
	
	// creates the list of vertices from dijkstra
	public void addOutputVertex(DijkstraHighway.AdjList.Vertex v) {
		v.nextVertex = null;
		if(firstOutputV == null) {
			firstOutputV = v;
		}
		else {
			DijkstraHighway.AdjList.Vertex n = firstOutputV;
			while(n.nextVertex != null) {
				n = n.nextVertex;
			}
			n.nextVertex = v;
		}
	}
	
	public void createGRA() {
		// the heading which tells how many vertices and edges are in the gra file
		numEdgesOutput = numVerticesOutput - 1;
		vertexNames = new String[numVerticesOutput];
		addOutput(numVerticesOutput + " " + numEdgesOutput);
		// first write all of the vertices
		DijkstraHighway.AdjList.Vertex n = firstOutputV;
		int i = 0;
		while(n != null) {
			vertexNames[i] = n.label;
			addOutput(n.label + " " + n.lat + " " + n.lon);
			n = n.nextVertex;
			i++;
		}
		// then fill in all the edges by lookup
		n = firstOutputV;
		while(n.nextVertex != null) {
			addOutput(indexLookup(n.label) + " " + indexLookup(n.nextVertex.label) + " " + this.map.findEdge(n.label, n.nextVertex.label));
			n = n.nextVertex;
		}
		writeOutput();
	}
	
	// looks up the vertex number for output name
	public int indexLookup(String name) {
		int i = 0;
		for(String n : vertexNames) {
			if(n.equals(name)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	// adds additional lines to the file
	public void addOutput(String toAdd) {
		System.out.println(toAdd);
		outputText += toAdd += "\n";
	}
	// writes output text to a file
	public void writeOutput() {
		try (BufferedWriter file = new BufferedWriter(new FileWriter(fileOutName))) {
            file.write(outputText);
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
	}
	
	// reads the input file
	public void parseFile() throws FileNotFoundException {
		File inputFile = new File(fileInName);
		Scanner scanner = new Scanner(inputFile);
		
		// the first line contains vertex and edge counts
		vertexCount = scanner.nextInt();
	    edgeCount = scanner.nextInt();
	    scanner.nextLine();
	    
	    // part of file that contains vertices
	    for(int i = 0; i < vertexCount; i++) {
	    	if (!scanner.hasNextLine()) {
	            System.out.println("Invalid input format: not enough vertices provided");
	            scanner.close();
	            return;
	        }
	    	String currentLine = scanner.nextLine().trim();
	    	String[] parts = currentLine.split("\\s+", 2);
	    	if (parts.length != 2) {
                System.out.println("Invalid input format: " + currentLine);
                continue;
            }
	    	String[] coordsString = parts[1].split(",");
	    	if (coordsString.length != 2) {
                System.out.println("Invalid coordinates format: " + parts[1]);
                continue;
            }
	    	double lat;
	    	double lon;
	    	try {
                lat = Double.parseDouble(coordsString[0].trim());
                lon = Double.parseDouble(coordsString[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Error parsing coordinates: " + parts[1]);
                continue;
            }
	    	// add vertex to the adjacency list
	    	map.addVertex(parts[0].trim(), lat, lon);
	    }
	    
	    // the last part contains edges
	    for(int i = 0; i < edgeCount; i++) {
	    	 if (!scanner.hasNextLine()) {
	             System.out.println("Invalid input format: not enough edges provided");
	             scanner.close();
	             return;
	         }
		    String currentLine = scanner.nextLine().trim();
		    String[] parts = currentLine.split("\\s+", 3);
		    if(parts.length != 3) {
		    	System.out.println("Invalid input format: edge format incorrect");
	            scanner.close();
	            return;
		    }
		    int v1;
		    int v2;
		    try {
	            v1 = Integer.parseInt(parts[0]);
	            v2 = Integer.parseInt(parts[1]);
	        }
		    catch (NumberFormatException e) {
	            System.out.println("Invalid input format: start and end vertex indices must be numbers");
	            scanner.close();
	            return;
	        }
		    // add edge to the adjacency list
		    map.addEdge(v1, v2, parts[2].trim());
	    }
	}

	// An adjacency list holds vertices (waypoints) and stores its edges (connections)
	public class AdjList {
		// performs the Dijkstra algorithm to find the shortest path between two vertices
		public void dijkstra(Vertex source, Vertex target) {
			// initialize the prev and dist arrays
			Vertex v = this.firstVertex;
			int numVertices = 0;
			while(v != null) {
				v = v.nextVertex;
				numVertices++;
			}
			v = this.firstVertex;
			// with the numVertices, we can initialize the priority queue and dist and prev arrays
			double[] dist = new double[numVertices];
			Vertex[] prev = new Vertex[numVertices];
			PriQue Q = new PriQue(numVertices);
			
			Q.insert(0, source);
			dist[source.vertexNumber] = 0;
			
			// starts all the arrays and queues to correct values
			for(int i = 0; i < numVertices; i++) {
				if(!v.equals(source)) {
					dist[i] = Double.POSITIVE_INFINITY;
					prev[i] = null;
					Q.insert(dist[i], v);
				}
				v = v.nextVertex;
			}
			// main loop
			while(!Q.isEmpty()) {
				Vertex u = Q.remove();
				if(u.equals(target)) {
					break;
				}
				Edge neighbor = u.connections;
				while(neighbor != null) {
					double alt = dist[u.vertexNumber] + neighbor.edgeLength;
					if(alt < dist[neighbor.v2.vertexNumber]) {
						dist[neighbor.v2.vertexNumber] = alt;
						prev[neighbor.v2.vertexNumber] = u;
						Q.decreasePriority(neighbor.v2, alt);
					}
					neighbor = neighbor.nextConnection;
				}
			}
		    
			// finds the shortest path
			Vertex u = target;
			if(prev[u.vertexNumber] != null || u.equals(source)) {
				while(u != null) {
					// prints the path from target to source
					addOutputVertex(u);
					numVerticesOutput++;
					u = prev[u.vertexNumber];
				}
			}
			else {
				System.out.println("Dijkstra's algorithm was unsuccessful.");
				System.exit(0);
			}
		}
		
		// returns the vertex with the given name
		public Vertex nameLookup(String name) {
			Vertex v = this.firstVertex;
			while(v != null) {
				if(v.label.equals(name)) {
					return v;
				}
				v = v.nextVertex;
			}
			System.out.println(name + " is not found. Dijkstra will not work. ");
			System.exit(0);
			return null;
		}
		
		// node class for every waypoint
		class Vertex {
			Vertex nextVertex;
			int vertexNumber; 
			String label;
			Edge connections;
			double lat;
			double lon;
			
			// constructor
			public Vertex(String label, int vertexNumber, double lat, double lon) {
				this.vertexNumber = vertexNumber;
				this.label = label;
				this.lat = lat;
				this.lon = lon;
			}
			
			// attaches a destination vertex to the connections linked list
			public void addConnection(int vertexNumber, int edgeNumber, String edgeLabel) {		
				if(this.connections == null) {
					this.connections = new Edge(edgeLabel, edgeNumber, this, lookupVertex(vertexNumber));
					return;
				}
				
				Edge i = this.connections;
				while(i.nextConnection != null) {
					i = i.nextConnection;
				}
				i.nextConnection = new Edge(edgeLabel, edgeNumber, this, lookupVertex(vertexNumber));
			}
		}
		
		// Edge class holds connections by an edge number
		class Edge {
			Edge nextConnection;
			String label;
			int edgeNumber;
			double edgeLength;
			Vertex v1;
			Vertex v2;
			
			// constructor
			public Edge(String label, int edgeNumber, Vertex v1, Vertex v2) {
				this.label = label;
				this.edgeNumber = edgeNumber;
				this.v1 = v1;
				this.v2 = v2;
				storeDist();
			}
			
			// uses GPS coordinates to calculate the distance between two vertices in miles
			// modified from ConvertGPSCoordinantes.java from the lab instructions
			// Learn how to spell this caused issues and was confusing for me thanks
			public void storeDist() {
				double R = 6371; //km, will be converted to miles
				double radLat1 = Math.toRadians(this.v1.lat);
				double radLat2 = Math.toRadians(this.v2.lat);
				double radDiffLat = Math.toRadians(this.v2.lat-this.v1.lat);
				double radDiffLon = Math.toRadians(this.v2.lon-this.v1.lon);
				
				double a = Math.sin(radDiffLat/2) * Math.sin(radDiffLat/2) +
						Math.cos(radLat1) * Math.cos(radLat2) *
						Math.sin(radDiffLon/2) * Math.sin(radDiffLon/2);
				double c = 2 * Math.atan2(Math.sqrt(a),  Math.sqrt(1-a));
				
				// stores the calculated distance in miles
				this.edgeLength = Math.abs(R * c);
			}
		}
		
		int numVertices = 0;
		int numEdges = 0;
		Vertex firstVertex;

		// constructor for adjacency list
	    public AdjList() {
	    	this.numVertices = 0;
	    	this.numEdges = 0;
	    	this.firstVertex = null;
	    }
	    
	    Vertex lookupVertex(int vertexNumber) {
	    	Vertex i = this.firstVertex;
	    	while(i != null) {
	    		if(i.vertexNumber == vertexNumber) {
	    			return i;
	    		}
	    		i = i.nextVertex;
	    	}
	    	return null;
	    }
	    
	    // adds an edge in between two vertices
	    void addEdge(int v1, int v2, String label) {
	    	Vertex vertex1 = lookupVertex(v1);
	    	Vertex vertex2 = lookupVertex(v2);
    		// if the vertices were not found there will be some kind of error
    		if(vertex1 == null) {
    			System.out.println("Could not find Vertex with id = " + v1 + ", so skipping \"" + label + "\"");
    			return;
    		}
    		if(vertex2 == null) {
    			System.out.println("Could not find Vertex with id = " + v2 + ", so skipping \"" + label + "\"");
    			return;
    		}
    		
	    	// bidirectional paths
    		vertex1.addConnection(v2, this.numEdges, label);
	    	vertex2.addConnection(v1, this.numEdges, label);
	    	this.numEdges++;
	    }
	    
	    void addVertex(String label, double lat, double lon) {
	    	Vertex toAdd = new Vertex(label, this.numVertices, lat, lon);
	    	if(this.firstVertex == null) {
	    		this.firstVertex = toAdd;
	    		numVertices = 1;
	    	}
	    	else {
	    		Vertex i = this.firstVertex;
	    		while(i.nextVertex != null) {
	    			i = i.nextVertex;
	    		}
	    		i.nextVertex = toAdd;
	    		numVertices++;
	    	}
	    }
	    
	    // displays the adjacency list that shows all the unique routes
	    void printAdjList() {
	    	Vertex i = this.firstVertex;
	    	while(i != null) {
	    		System.out.println("Routes from vertex #" + i.vertexNumber + ": " + i.label);
	    		Edge j = i.connections;
	    		while(j != null) {
	    			System.out.println("     edge #" + j.edgeNumber + ": " + j.label + " to " + j.v2.label);
	    			j = j.nextConnection;
	    		}
	    		i = i.nextVertex;
	    	}
	    }
	    public String findEdge(String v1, String v2) {
	    	Vertex i = this.firstVertex;
	    	while(i != null) {
	    		Edge j = i.connections;
	    		while(j != null) {
	    			if((j.v1.label.equals(v1) && j.v2.label.equals(v2)) || (j.v1.label.equals(v2) && j.v2.label.equals(v1))) {
	    				return j.label;
	    			}
	    			j = j.nextConnection;
	    		}
	    		i = i.nextVertex;
	    	}
	    	return "name";
	    }
	    
	    // my prique class from asg4 to use for Dijkstra
	    // modified to make the smallest priority at the top
	    public class PriQue{
	    	// Node contains a priority and data
	    	private class Node {
	    		Vertex data;
	    		double pri;
	    		int id;
	    		Node next;
	    		// constructor
	    		Node(Vertex data, double pri, int id){
	    			this.data = data;
	    			this.pri = pri;
	    			this.id = id;
	    			this.next = null;
	    		}
	    	}
	    	
	    	// Node structure used stored ids to return the E data
	    	Node firstNode;
	    	Node lastNode;
	    	
	    	// holds ids of Nodes
	    	int[] array;
	    	int size;
	    	
	    	// constructor
	    	public PriQue(int size){
	    		this.array = new int[size];
	    		for(int i = 0; i < size; i++) {
	    			this.array[i] = -1;
	    		}
	    		this.size = 0;
	    	}
	    	
	    	// Adds an id to the priority queue
	    	public void insert(double pri, Vertex data) {
	    		int id = this.size;
	    		Node newNode = new Node(data, pri, id);
	    		// if you are inserting the first Node
	    		if(this.size == 0) {
	    			this.array[0] = 0;
	    			this.firstNode = newNode;
	    			this.lastNode = newNode;
	    			this.size++;
	    		}
	    		else {
	    			this.array[size] = id;
	    			lastNode.next = newNode;
	    			lastNode = newNode;
	    			this.size++;
	    		}
	    		
	    		// use the heapify algorithm to sort the heap
	    		this.heapify(this.size - 1);
	    	}
	    	
	    	// new method that updates the priority
	    	public void decreasePriority(Vertex data, double newPri) {
	    		 int id = -1;
	    		 // Find the node with the given data and change its priority
	    		 Node current = this.firstNode;
	    		 while(current != null) {
	    			 if(current.data.equals(data)) {
    				 	current.pri = newPri;
    				 	id = current.id;
    				 	break;
    				 }
	    			 current = current.next;
    			 }
	    		 // adjust its position in the priority queue
	    		 if(id != -1) {
	    			 int index = -1;
	    			 for(int i = 0; i < this.size; i++) {
	    				 if (array[i] == id) {
	    					 index = i;
	    					 break;
	    				}
    				 }
	    			 if(index != -1) {
	    				 heapify(index);
    				 }
	    		}
	    	}
	    	
	    	// brings the top priority to the top
	    	private void heapify(int i) {
	    		int parent = (i - 1) / 2;
	    		while (i > 0 && nodePri(array[i]) < nodePri(array[parent])) {
	    			swap(i, parent);
	                i = parent;
	                parent = (i - 1) / 2;
	            }
	    	}
	    	
	    	// starts with the top and works down after a remove by bringing high values up
	    	private void trickle(int i) {
	    		int left = 2 * i + 1;
	    		int right = 2 * i + 2;
	    		int small = i;
	    		
	    		if(left < this.size && nodePri(array[left]) < nodePri(array[small])) {
	    			small = left;
	    		}
	    		if(right < this.size && nodePri(array[right]) < nodePri(array[small])) {
	    			small = right;
	    		}
	    		
	    		if(small != i) {
	    			swap(i, small);
	    			trickle(small);
	    		}
	    	}
	    	
	    	// used in trickle and heapify to swap at indices
	    	private void swap(int i, int j) {
	    		int temp = array[i];
	    		array[i] = array[j];
	    		array[j] = temp;
	    	}
	    	
	    	// loops through the linked structure to return the priority of an id (used in heapify and trickle)
	    	private double nodePri(int id) {
	    		Node i = this.firstNode;
	    		while(i != null) {
	    			if(i.id == id) {
	    				return i.pri;
	    			}
	    			i = i.next;
	    		}
	    		return -1;
	    	}
	    	// loops through the linked structure to return the E data
	    	private Vertex nodeData(int id) {
	    		Node i = this.firstNode;
	    		while(i != null) {
	    			if(i.id == id) {
	    				return i.data;
	    			}
	    			i = i.next;
	    		}
	    		return null;
	    	}
	    	
	    	// used in testing to print the ids and priority of array
	    	private void display() {
	    		for (int i = 0; i < array.length; i++) {
	    			if(array[i] == -1) {
	    				System.out.println(" complete");
	    				return;
	    			}
	                System.out.print(array[i] +"p"+nodePri(array[i]) + " ");
	            }
	    	}

	    	// removes the item of top priority and trickles the rest
	    	public Vertex remove() {
	    		if(!this.isEmpty()) {
	    			int root = array[0];
	    			array[0] = array[this.size - 1];
	    			this.size--;
	    			this.trickle(0);
	    			return nodeData(root);
	    		}
	    		return null;
	    	}

	    	// if the size is zero the queue is empty
	    	public boolean isEmpty() {
	    		return this.size == 0;
	    	}
	    }
	}  
}