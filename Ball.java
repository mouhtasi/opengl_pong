public class Ball
{
	private Vector2 position = new Vector2();
	private Vector2 velocity = new Vector2();
	private Color color = new Color();
	private double radius;

	/**Create a Ball object with radius of 1.**/
	public Ball()
	{
		radius = 1.0;
	}

	/**Return the position of the ball.**/
	public Vector2 getPosition()
	{
		return position.get();
	}

	/**Return the velocity of the ball.**/
	public Vector2 getVelocity()
	{
		return velocity.get();
	}

	/**Return the color of the ball.**/
	public Color getColor()
	{
		return color.get();
	}

	/**Return the radius of the ball.**/
	public double getRadius()
	{
		return radius;
	}

	/**Set the position of the ball to doubles x and y.**/
	public void setPosition(double x, double y)
	{
		position.set(x, y);
	}

	/**Set the velocity of the ball.**/
	public void setVelocity(double x, double y)
	{
		velocity.set(x, y);
	}

	/**Set the color of the ball.**/
	public void setColor(float r, float g, float b)
	{
		color.set(r, g, b);
	}

	/**Set the radius of the ball.**/
	public void setRadius(double radius)
	{
		this.radius = radius;
	}

	/**Override the String formatting.**/
	public String toString()
	{
		return "Ball(position=" + position + ",\n" +
		       "     velocity=" + velocity + ",\n" +
		       "     color="    + color    + ",\n" +
		       "     radius="   + radius   + ")"; 
	}

	/**Print information about the ball.**/
	public void printSelf()
	{
		System.out.println(this);
	}

	/**Fire the ball from it's current position at the given speed.
	Will either shoot towards the player or the computer.**/
	public void fire(double x, double y, double speed)
	{
		position.x = x;
		position.y = y;

		if(Math.random() * 2 < 1.0) velocity.x = -(Math.random()* 0.5 + 0.5);
		else velocity.x = Math.random()* 0.5 + 0.5;
		velocity.y = Math.random()* 2 - 1;


		velocity.normalize();
		velocity.scale(speed);
	}

	/**Update the ball.**/
	public void update(double frameTime)
	{
		position.x += velocity.x * frameTime;
		position.y += velocity.y * frameTime;
	}

}
