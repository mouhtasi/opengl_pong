public class Paddle
{
	private Vector2 position = new Vector2();
	private Color color = new Color();
	private double speed;
	private double width;
	private double height;
	private boolean movingUp;
	private boolean movingDown;
	private double maxY;
	private double minY;

	/**Create a Paddle object.**/
	public Paddle()
	{
	}

	/**Set the coordinates to x, y.**/
	public void setPosition(double x, double y)
	{
		position.set(x, y);
	}

	/**Set the coordinated to as Vector2 object vec.**/
	public void setPosition(Vector2 vec)
	{
		position.set(vec);
	}

	/**Set the color to RGB values r, g, b.**/
	public void setColor(float r, float g, float b)
	{
		color.set(r, g, b);
	}

	/**Set the color as Color object c.**/
	public void setColor(Color c)
	{
		color.set(c);
	}

	/**Set the speed to specified speed.**/
	public void setSpeed(double speed)
	{
		this.speed = speed;
	}

	/**Set the width to specified width.**/
	public void setWidth(double width)
	{
		this.width = width;
	}

	/**Set the height to specified height.**/
	public void setHeight(double height)
	{
		this.height = height;
	}

	/**Return the position.**/
	public Vector2 getPosition()
	{
		return position.get();
	}

	/**Return the color.**/
	public Color getColor()
	{
		return color.get();
	}

	/**Return the speed.**/
	public double getSpeed()
	{
		return speed;
	}

	/**return the width.**/
	public double getWidth()
	{
		return width;
	}

	/**Return the height.**/
	public double getHeight()
	{
		return height;
	}

	/**Override the string formatting.**/
	public String toString()
	{
		return "Paddle(position=" + position + ",\n" +
		       "       color="    + color    + ",\n" +
		       "       speed="    + speed    + ",\n" +
		       "       width="    + width    + ",\n" +
		       "       height="   + height   + ")";
	}

	/**Prints information about the paddle.**/
	public void printSelf()
	{
		System.out.println(this);
	}

	/**Set movingUp to flag.**/
	public void setMovingUp(boolean flag)
	{
		movingUp = flag;   
	}

	/**Set movingDown to flag.**/
	public void setMovingDown(boolean flag)
	{
		movingDown = flag;
	}

	/**Return a boolean representing whether paddle is moving up.**/
	public boolean isMovingUp()
	{
		return movingUp;
	}

	/**Return a boolean representing whether paddle is moving up.**/
	public boolean isMovingDown()
	{
		return movingDown;
	}

	/**Set the max and min bounds.**/
	public void setBounds(double min, double max)
	{
		minY = min;
		maxY = max;
	}

	/**Update the data for the paddle.**/
	public void update(double deltaTime)
	{
		if(movingUp)
		{
			position.y += deltaTime * speed;
			if(position.y > maxY)
			{
				position.y = maxY;
				movingUp = false;
			}
		}
		if(movingDown)
		{ 
			position.y -= deltaTime * speed;

			if(position.y < minY)
			{
				position.y = minY;
				movingDown = false;
			}    
		}
	}
}