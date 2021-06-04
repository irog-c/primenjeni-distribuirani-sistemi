package domaci;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Domaci implements Watcher
{
	private static String host = "localhost:2181";
	private static int votesCast = 0;
	private static int createdNodes = 0;
	
	private ZooKeeper zk;
	private boolean isLeader = false;
	private String id;
	private Random random;
	private String generatedVote;
	
	private boolean nodeExists(String path) throws KeeperException, InterruptedException
	{
		Stat existStat = zk.exists(path, true);
		if(existStat != null)
			return true;
		else
			return false;
	}
	
	Domaci()
	{
		random = new Random();
		int idInteger = random.nextInt();
		id = Integer.toHexString(idInteger);
	}
	
	public void start() throws IOException
	{
		//System.out.println("Node with ID: " + id + " started.");
		zk = new ZooKeeper(host, 500, this);
	}
	
	public String generateVote()
	{
		int genInteger = random.nextInt();
		
		if((genInteger % 2) == 0)
		{
			//System.out.println("Node with ID: " + id + " generated Yes.");
			return "Yes";
		}
		else
		{
			//System.out.println("Node with ID: " + id + " generated No.");
			return "No";
		}
	}
	
	public void register() throws KeeperException, InterruptedException
	{
		if(!nodeExists("/leader"))
		{
			//System.out.println("Node with ID " + id + " has been made leader.");
			zk.create("/leader", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			zk.getData("/leader", true, null); // Put watcher
			zk.getData("/votes", true, null); // Put watcher
			isLeader = true;
		}
		
		if(!nodeExists("/votes"))
			System.out.println("Node /votes does not exist!");
		else
		{
			createdNodes++;
			if(createdNodes == 4)
			{
				zk.setData("/leader", "start".getBytes(), -1);
			}
		}
	}
	
	public void stop() throws Exception
	{
		zk.close();
	}

	@Override
	public void process(WatchedEvent event) 
	{
		//System.out.println("Event generated: " + event);
		if(event.getType() == Event.EventType.NodeDataChanged)
		{
			// Ako je node koji handle-uje ovaj event nije tipa Leader
			if(!isLeader)
			{
				// Ako je leader upisao "start" logika + odlucivanje ko je pobedio logika
				if(event.getPath().equals("/leader"))
				{
					try 
					{
						String data = new String(zk.getData("/leader", true, null));
						if(data.equals("start"))
						{
							//System.out.println("Generating vote...");
							generatedVote = generateVote();
							
							String votePath = zk.create("/votes/vote-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
							zk.getData(votePath, true, null); // Stavi watcher
							zk.setData(votePath, generatedVote.getBytes(), 0);
							
							//System.out.println("Vote path: " + votePath);
							
							//Thread.sleep(200);
							votesCast++;
							//System.out.println("Votes currently cast: " + votesCast);
							
							if(votesCast == 3)
							{
								try 
								{
									int votesYes = 0;
									int votesNo = 0;
									
									List<String> votes = zk.getChildren("/votes", true);
									
									for(String vote : votes)
									{
										//System.out.println("\tGetting vote: /votes/" + vote);
										String voteData = new String(zk.getData("/votes/" + vote, true, null));
										
										if(voteData.equals("Yes"))
											votesYes++;
										else
											votesNo++;
									}
									
									if(votesYes > votesNo)
									{
										System.out.println("FINAL RESULT: Yes");
										zk.setData("/leader", "Yes".getBytes(), -1);
									}
									else
									{
										System.out.println("FINAL RESULT: No");
										zk.setData("/leader", "No".getBytes(), -1);
									}
								} 
								catch (KeeperException e) 
								{
									e.printStackTrace();
								} 
								catch (InterruptedException e) 
								{
									e.printStackTrace();
								}
							}
						}
						else
						{
							if(data.equals(generatedVote))
							{
								//System.out.println(data + " == " + generatedVote);
								System.out.println(id + ": Moj glas je pobedio!");
							}
							else
							{
								//System.out.println(data + " == " + generatedVote);
								System.out.println(id + ": Moj glas je izgubio...");
							}
						}
					} 
					catch (KeeperException e) 
					{
						e.printStackTrace();
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}
