public class Color
{
	// member data fields
	public float red;
	public float green;
	public float blue;

	/**Create a new Color object which is white.**/
	public Color()
	{
		set(1, 1, 1);
	}

	/**Create a new Color object with rgb values r, g, b.**/
	public Color(float r, float g, float b)
	{
		set(r, g, b);
	}

	/**Create a new Color object with rgb values of Color object c.**/
	public Color(Color c)
	{
		this(c.red, c.green, c.blue);   // call another ctor
	}

	/**Set the color to rgb values r, g, b.**/
	public void set(float r, float g, float b)
	{
		// clamp before assign
		red = clamp(r);
		green = clamp(g);
		blue = clamp(b);
	}

	/**Set the color to the rgb values from Color object c.**/
	public void set(Color c)
	{
		set(c.red, c.green, c.blue);
	}

	/**Return the Color object.**/
	public Color get()
	{
		return new Color(this);
	}

	/**Override the string formatting.**/
	public String toString()
	{
		return String.format("Color(red=%1.3f, green=%1.3f, blue=%1.3f)",
		                     red, green, blue);
	}

	/**Print the rgb values.**/
	public void printSelf()
	{
		System.out.println(this);
	}

	/**Limit the color values between 0 and 1.**/
	private float clamp(float value)
	{
		if(value < 0)
			value = 0;      // cannot be less than 0
		else if(value > 1)
			value = 1;      // cannot be greater than 1
		return value;
	}
}
