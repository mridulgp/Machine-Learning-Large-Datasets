//package mlld.graphsampling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Conductance
{
	public Conductance(String seed)
	{
		edgesHash = new HashMap<String, String>();
		boundary = new HashSet<String>();
		pageRankVector = new HashMap<String, Double>();
		S = new HashSet<String>();
		subS = new HashSet<String>();
		this.seed = seed;
	}

	private void readPageRanks(BufferedReader inReader) throws IOException
	{
		while(inReader.ready())
		{
			String line = inReader.readLine();

			line = line.trim();
			String[] nodeInfo = line.split("\t");

			if(nodeInfo.length>1)
			{
				double weight = Double.parseDouble(nodeInfo[1]);
				pageRankVector.put(nodeInfo[0], weight);
			}
		}
	}

	private void readNodeEdges(BufferedReader inReader) throws IOException
	{
		totalEdges = 0;
		while(inReader.ready())
		{
			totalEdges++;
			String line = inReader.readLine();

			line = line.trim();
			String[] nodeInfo = line.split("\t", 2);

			if(pageRankVector.get(nodeInfo[0])!=null)
			{
				if(nodeInfo.length>1)
				{
					edgesHash.put(nodeInfo[0], nodeInfo[1]);
				}
				/*else
					edgesHash.put(nodeInfo[0], "");*/
			}
		}
	}

	private double computeConductance(Set<String> sampleSet)
	{
		double conductance = 0.0;
		volume = 0;
		boundary.clear();
		Iterator it = sampleSet.iterator();

		while (it.hasNext())
		{
			String node = (String) it.next();
			if(edgesHash.get(node)!=null)
			{
				String nodeInfo = edgesHash.get(node);
				if(!nodeInfo.equals(""))
				{
					String[] neighbors = edgesHash.get(node).split("\t");
					volume = volume + neighbors.length;

					for(int i=0; i<neighbors.length; i++)
					{
						if(!sampleSet.contains(neighbors[i]))
							boundary.add(neighbors[i]);
					}
				}
			}
		}

		conductance = ((double) boundary.size())/( (double)Math.min(volume, totalEdges - volume) );

		if(conductance<=1)
			return conductance;
		else
			return 0;
	}

	@SuppressWarnings("unchecked")
	public Set<String> minimalConductance(BufferedReader inReader, BufferedReader inReader1) throws IOException
	{
		readPageRanks(inReader);
		readNodeEdges(inReader1);

		S.add(seed);
		subS.add(seed);

		@SuppressWarnings("rawtypes")
		ArrayList sortedPageRank = new ArrayList( pageRankVector.entrySet() );  

		Collections.sort( sortedPageRank , new Comparator() {  
			public int compare( Object o1 , Object o2 )  
			{
				Map.Entry e1 = (Map.Entry)o1 ;
				Map.Entry e2 = (Map.Entry)o2 ;
				Double first = (Double)e1.getValue();
				Double second = (Double)e2.getValue();
				return first.compareTo( second );
			}
		});

		Collections.reverse(sortedPageRank);
		Iterator i = sortedPageRank.iterator();
		while ( i.hasNext() )  
		{  
			Map.Entry entry = (Map.Entry) i.next();
			String node = (String)entry.getKey();
			double weight = (Double)entry.getValue();

			//System.out.println("Sorted: " + node + "\t" + weight);

			if(!node.equals(seed))
			{
				S.add(node);
				//System.out.println(S.size() + "\t" + subS.size());
				double conductance1 = computeConductance(S);
				double conductance2 = computeConductance(subS);

				//System.out.println(conductance1 + "\t" + conductance2);

				if(conductance1 < conductance2)
				{
					Iterator it = S.iterator();
					while (it.hasNext())
					{
						String key = (String) it.next();
						subS.add(key);
					}
				}
			}
		}

		return subS;
	}

	public Map<String, Double> getPageRankVector()
	{
		return pageRankVector;
	}

	public Map<String, String> getEdgesHash()
	{
		return edgesHash;
	}

	protected Map<String, String> edgesHash;
	protected Map<String, Double> pageRankVector;
	protected Set<String> boundary;
	protected Set<String> S;
	protected Set<String> subS;
	protected int volume;
	protected int totalEdges;
	protected String seed;

	public static void main(String[] args) throws IOException
	{
		String seed = args[0];

		// page ranks
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));

		// graph file
		BufferedReader inReader1 = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));

		BufferedWriter out = new BufferedWriter(new FileWriter(args[3]));

		Conductance conductance = new Conductance(seed);
		Set<String> sampledGraph = conductance.minimalConductance(inReader, inReader1);

		Map<String, Double> pageRankVector = conductance.getPageRankVector();
		Map<String, String> edgesHash = conductance.getEdgesHash();

		Iterator it = sampledGraph.iterator();
		while (it.hasNext())
		{
			String node = (String) it.next();
			if(pageRankVector.get(node)!=null)
			{
				System.out.println(node + "\t" + pageRankVector.get(node));
			}
			if(edgesHash.get(node)!=null)
			{
				String adjacencyList = node;
				String[] neighbors = edgesHash.get(node).split("\t");
				for(int i=0; i<neighbors.length; i++)
				{
					if(pageRankVector.containsKey(neighbors[i]))
						adjacencyList = adjacencyList + "," + neighbors[i].replaceAll("\\,", "_");
				}
				if(adjacencyList.split("\\,").length>1)
				{
					out.write(adjacencyList);
					out.write("\n");
				}
			}
		}

		out.close();
	}
}
