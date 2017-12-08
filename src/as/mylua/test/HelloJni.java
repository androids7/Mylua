
package as.mylua.test;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;

import org.keplerproject.luajava.Console;
import java.io.*;
import android.widget.*;
import org.keplerproject.luajava.*;
public class HelloJni extends Activity
{
	ByteArrayOutputStream bao;
	PrintStream ps;
	ScrollView scrollview;
	
	LuaState luastate;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		bao=new ByteArrayOutputStream();
		ps=new PrintStream(bao);
		System.setOut(ps);
		System.setErr(ps);
		scrollview=new ScrollView(this);
        TextView  tv = new TextView(this);
        
		scrollview.addView(tv);
		
		/*
		Console mconsole=new Console();
		String[] args={"/sdcard/.0/mytest.lua"};
		mconsole.main(args);
		*/
		
		String[] args={"/sdcard/.0/mytest.lua"};
		run(args);
		
		
		
		
		
		
		tv.setText(new String(bao.toByteArray()));
		//tv.setText( stringFromJNI() );
      
		setContentView(scrollview);
    }
	
/*
   
    public native String  stringFromJNI();
*/
	
    static {
        System.loadLibrary("luajava-apklib");
    }
	
	
	
	private LuaException _lastScriptErr;

	public void onErrorHandle(String msg)
	{
		//Log.error("Lua Stack: {}",msg);
		_lastScriptErr = new LuaException(msg);
	}
	
	
	
	public void run(String[] args)
	{
		
		try
		{
			luastate = LuaStateFactory.newLuaState();
			luastate.openLibs();

			JavaFunction toast=new JavaFunction(luastate)
			{

				@Override
				public int execute() throws LuaException 
				{

					String msg = getParam(2).getString();
					if (msg == null)
					{
						throw new LuaException("the error msg is not string");
					}
					onErrorHandle(msg);
					Toast.makeText(getBaseContext(),msg,0).show();
					return 0;
				}
			};
			
			toast.register("print");
			
			
			if (args.length > 0)
			{
				for (int i = 0; i < args.length; i++)
				{
					int res = luastate.LloadFile(args[i]);
					if (res == 0)
					{
						res = luastate.pcall(0, 0, 0);
					}
					if (res != 0)
					{
						throw new LuaException("Error on file: " + args[i] + ". " + luastate.toString(-1));
					}
				}

				return;
			}

			System.out.println("API Lua Java - console mode.");

			BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));

			String line;

			System.out.print("> ");
			while ((line = inp.readLine()) != null && !line.equals("exit"))
			{
				int ret = luastate.LloadBuffer(line.getBytes(), "from console");
				if (ret == 0)
				{
					ret = luastate.pcall(0, 0, 0);
				}
				if (ret != 0)
				{
					System.err.println("Error on line: " + line);
					System.err.println(luastate.toString(-1));
				}
				System.out.print("> ");
			}

			luastate.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
	
}
