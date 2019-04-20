package bhestie.levpos.gui;


import bhestie.levpos.State;

import javax.swing.*;
import java.awt.*;

public abstract class Background extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Image background;
	protected Image black;
	protected Image white;
	protected Image king;
	protected State aState;
	
	public Background()
	{
		super();
	}
	
	public State getaState() {
		return aState;
	}
	
	public void setaState(State aState) {
		this.aState = aState;
	}
	
	@Override
	public void paint( Graphics g ) {
	    /*
	    super.paint(g);
	    g.drawImage(background, 10, 30, null);
	    for(int i=0;i<this.aState.getBoard().length;i++)
	    {
	    	for(int j=0;j<this.aState.getBoard().length;j++)
	    	{
	    		if(this.aState.getPawn(i, j) == 'B')
	    		{
	    			int posX= 34 + (i*37);
	    			int posY= 12 + (j*37);
	    			g.drawImage(black, posY, posX,null);
	    		}	
	    		if(this.aState.getPawn(i, j) == 'W')
	    		{
	    			int posX= 35 + (i*37);
	    			int posY= 12 + (j*37);
	    			g.drawImage(white, posY, posX,null);
	    		}	
	    		if(this.aState.getPawn(i, j) == 'K')
	    		{
	    			int posX= 34 + (i*37);
	    			int posY= 12 + (j*37);
	    			g.drawImage(king, posY, posX,null);
	    		}	
	    	}
	    }
	    g.dispose();
	    */
	}

}
