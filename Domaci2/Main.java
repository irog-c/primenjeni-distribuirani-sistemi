package domaci;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main 
{

	public static void main(String[] args) throws Exception
	{
		Logger.getRootLogger().setLevel(Level.OFF);
		List<Domaci> domaci = new ArrayList<>();
		
		for(int i = 0; i < 4; ++i)
			domaci.add(new Domaci());
		
		for(Domaci d : domaci)
		{
			d.start();
			d.register();
		}
		
		Thread.sleep(120000000);
		
		for(Domaci d : domaci)
			d.stop();
	}

}
