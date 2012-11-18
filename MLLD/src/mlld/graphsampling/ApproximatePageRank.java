//package mlld.graphsampling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ApproximatePageRank
{
	public ApproximatePageRank(String seed)
	{
		this.seed = seed;
		pageRankVector = new HashMap<String, Double>();
		residualVector = new HashMap<String, Double>();
		residualVector.put(this.seed, 1.0);
		nodeDegree = 0;
		converged = true;
	}

	public ApproximatePageRank(String seed, double alpha, double epsilon)
	{
		this.seed = seed;
		pageRankVector = new HashMap<String, Double>();
		residualVector = new HashMap<String, Double>();
		residualVector.put(this.seed, 1.0);
		this.alpha = alpha;
		this.epsilon = epsilon;
		nodeDegree = 0;
		converged = true;
	}

	private String[] processNode(String line, int iteration) throws IOException
	{
		//line = line.trim();
		String[] nodeInfo = line.split("\t");
		node = nodeInfo[0];

		nodeDegree = nodeInfo.length-1;
		neighbors = new String[nodeDegree];

		for (int i=1; i<nodeInfo.length; i++)
		{
			neighbors[i-1] = nodeInfo[i];
		}

		return neighbors;
	}

	private void push()
	{
		double weight = 0.0;
		double residualWeight = 0.0;

		if(pageRankVector.get(node)!=null)
			weight = pageRankVector.get(node);

		if(residualVector.get(node)!=null)
			residualWeight = residualVector.get(node);

		for (int i=0; i<neighbors.length; i++)
		{
			if(neighbors[i]!=null)
			{
				String neighbor = neighbors[i];
				double neighborWeight = 0.0;

				if(residualVector.get(neighbor)!=null)
					neighborWeight = residualVector.get(neighbor);

				if(nodeDegree>0)
					neighborWeight = neighborWeight + (1.0-alpha)*(residualWeight/(2*nodeDegree));
				
				residualVector.put(neighbor, neighborWeight);
			}
		}

		weight = weight + alpha*residualWeight;
		residualWeight = (1.0-alpha)*residualWeight/2;
		
		pageRankVector.put(node, weight);
		residualVector.put(node, residualWeight);

	}

	public Map<String, Double> pageRank(BufferedReader inReader, int iteration) throws IOException
	{
		int count = 0;
		converged = true;

		while(inReader.ready())
		{
			String line = inReader.readLine();
			processNode(line, iteration);

			String[] nodeInfo = line.split("\t");
			node = nodeInfo[0];

			double r = 0.0;
			if(residualVector.get(node)!=null)
				r = residualVector.get(node);

			if(nodeDegree>0)
			{
				if( (r/nodeDegree) > epsilon)
				{
					//System.out.println(node + "\t" + r/nodeDegree);
					converged = false;
					push();
					//inReader.close();
					//break;
				}
			}

			count++;
		}

		return pageRankVector;
	}

	public boolean isConverged()
	{
		return converged;
	}

	public Map<String, Double> getPageRankVector()
	{
		return pageRankVector;
	}

	public Map<String, Double> getResidualVector()
	{
		return residualVector;
	}

	protected Map<String, Double> pageRankVector;
	protected Map<String, Double> residualVector;
	protected String seed;
	protected String node;
	protected String[] neighbors;
	protected int nodeDegree;
	protected double alpha;
	protected double epsilon;
	protected boolean converged;

	public static void main(String[] args) throws IOException
	{
		String seed = args[1];
		double alpha = Double.parseDouble(args[2]);
		double epsilon = Double.parseDouble(args[3]);

		ApproximatePageRank apr = new ApproximatePageRank(seed, alpha, epsilon);

		int iteration=0;
		Map<String, Double> pageRankVector = new HashMap<String, Double>();
		Map<String, Double> residualVector = new HashMap<String, Double>();

		while(true)
		{
			iteration++;
			BufferedReader inReader = new BufferedReader(new FileReader(args[0]));

			pageRankVector = apr.pageRank(inReader, iteration);			
			residualVector = apr.getResidualVector();

			/*double pweight = 0;
			double rweight = 0;
			Iterator it = pageRankVector.entrySet().iterator();

			while (it.hasNext())
			{
				Map.Entry entry = (Map.Entry) it.next();
				pweight = pweight + (Double)entry.getValue();
			}

			Iterator it1 = residualVector.entrySet().iterator();

			while (it1.hasNext())
			{
				Map.Entry entry = (Map.Entry) it1.next();
				rweight = rweight + (Double)entry.getValue();
			}

			System.out.println("p+r= " + (pweight+rweight));*/

			inReader.close();

			if(apr.isConverged())
			{
				//System.out.println("Iteration = " + iteration);
				break;
			}
		}

		Iterator it = pageRankVector.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();
			String node = (String)entry.getKey();
			double weight = (Double)entry.getValue();

			System.out.println(node + "\t" + weight);
		}
	}
}
