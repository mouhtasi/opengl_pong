import java.io.*;
import javax.sound.sampled.*;

public class SoundPlayer
{
	private File file;
	private AudioInputStream inputStream;
	private AudioFormat audioFormat;
	private SourceDataLine sourceDataLine;
	private boolean playing;

	/**Start playing sound.**/
	public void play(String fileName)
	{
		// stop previous playback
		stop();

		// open the file
		open(fileName);

		// create a thread and play the sound async
		SoundPlayThread thread = new SoundPlayThread();
		thread.start();
	}

	/**Stop playing sound.**/
	public void stop()
	{
		playing = false;
	}

	/**Open a sound file.**/
	private void open(String fileName)
	{
		try
		{
			file = new File(fileName);
			if(!file.exists())
			{ 
				System.out.println("[ERROR] Cannot find file: " + fileName);
				return;
			}

			inputStream = AudioSystem.getAudioInputStream(file);
			audioFormat = inputStream.getFormat();

			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			sourceDataLine = (SourceDataLine)AudioSystem.getLine(info);
		}
		catch(Exception e)
		{ 
			e.printStackTrace();
			return;
		}
	}

	// inner class, thread for playing audio stream data
	class SoundPlayThread extends Thread
	{
		private static final int BUFFER_SIZE = 128 * 1024;
		private byte buffer[] = new byte[BUFFER_SIZE];

		// start thread 
		public void run()
		{
			try
			{
				sourceDataLine.open(audioFormat);
				sourceDataLine.start();

				// loop
				playing = true;
				int dataCount = 0;  // # of bytes
				while(dataCount != -1 || playing)
				{
					dataCount = inputStream.read(buffer, 0, buffer.length);
					if(dataCount >= 0) 
						sourceDataLine.write(buffer, 0, dataCount);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				sourceDataLine.drain();
				sourceDataLine.close();
				playing = false;
			}
		}
	}
}
