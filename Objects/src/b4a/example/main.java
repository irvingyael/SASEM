package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public static int _casos = 0;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_splash = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_bienvenido = null;
public anywheresoftware.b4a.objects.drawable.BitmapDrawable _bmplogo = null;
public anywheresoftware.b4a.objects.drawable.BitmapDrawable _bmplogo2 = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_pregunta = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_nuevo = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_registrado = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_general = null;
public anywheresoftware.b4a.objects.IntentWrapper _intento = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 33;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 36;BA.debugLine="bmpLogo.Initialize(LoadBitmap(File.DirAssets,\"log";
mostCurrent._bmplogo.Initialize((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"logo.png").getObject()));
 //BA.debugLineNum = 37;BA.debugLine="bmpLogo2.Initialize(LoadBitmap(File.DirAssets,\"lo";
mostCurrent._bmplogo2.Initialize((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"logo_fondo.png").getObject()));
 //BA.debugLineNum = 38;BA.debugLine="intento.Initialize(intento.ACTION_VIEW, \"https://";
mostCurrent._intento.Initialize(mostCurrent._intento.ACTION_VIEW,"https://play.google.com/store/search?q=qr&hl=es_419");
 //BA.debugLineNum = 39;BA.debugLine="dibujar_splash";
_dibujar_splash();
 //BA.debugLineNum = 40;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 46;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 48;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 42;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 44;BA.debugLine="End Sub";
return "";
}
public static anywheresoftware.b4a.objects.drawable.StateListDrawable  _buttongradient(int _lightcolor,int _darkcolor) throws Exception{
anywheresoftware.b4a.agraham.reflection.Reflection _ref = null;
anywheresoftware.b4a.objects.drawable.ColorDrawable _gdwenabled = null;
anywheresoftware.b4a.objects.drawable.ColorDrawable _gdwpressed = null;
anywheresoftware.b4a.objects.drawable.ColorDrawable _gdwdisabled = null;
anywheresoftware.b4a.objects.drawable.StateListDrawable _stdgradient = null;
 //BA.debugLineNum = 106;BA.debugLine="Sub ButtonGradient(LightColor As Int, DarkColor As";
 //BA.debugLineNum = 107;BA.debugLine="Dim ref As Reflector";
_ref = new anywheresoftware.b4a.agraham.reflection.Reflection();
 //BA.debugLineNum = 109;BA.debugLine="Dim gdwEnabled As ColorDrawable";
_gdwenabled = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 110;BA.debugLine="gdwEnabled.Initialize(LightColor,15)";
_gdwenabled.Initialize(_lightcolor,(int) (15));
 //BA.debugLineNum = 112;BA.debugLine="Dim gdwPressed As ColorDrawable";
_gdwpressed = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 113;BA.debugLine="gdwPressed.Initialize(DarkColor,15)";
_gdwpressed.Initialize(_darkcolor,(int) (15));
 //BA.debugLineNum = 114;BA.debugLine="ref.Target = gdwPressed";
_ref.Target = (Object)(_gdwpressed.getObject());
 //BA.debugLineNum = 115;BA.debugLine="ref.RunMethod4(\"setCornerRadii\", Array As Objec";
_ref.RunMethod4("setCornerRadii",new Object[]{(Object)(new float[]{(float) (10),(float) (15),(float) (10),(float) (15),(float) (10),(float) (15),(float) (10),(float) (15)})},new String[]{"[F"});
 //BA.debugLineNum = 117;BA.debugLine="Dim gdwDisabled As ColorDrawable";
_gdwdisabled = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 118;BA.debugLine="gdwDisabled.Initialize(Colors.Gray,15)";
_gdwdisabled.Initialize(anywheresoftware.b4a.keywords.Common.Colors.Gray,(int) (15));
 //BA.debugLineNum = 120;BA.debugLine="Dim stdGradient As StateListDrawable";
_stdgradient = new anywheresoftware.b4a.objects.drawable.StateListDrawable();
 //BA.debugLineNum = 121;BA.debugLine="stdGradient.Initialize";
_stdgradient.Initialize();
 //BA.debugLineNum = 122;BA.debugLine="stdGradient.AddState2(Array As Int(stdGradient.";
_stdgradient.AddState2(new int[]{_stdgradient.State_Enabled,(int) (-_stdgradient.State_Pressed)},(android.graphics.drawable.Drawable)(_gdwenabled.getObject()));
 //BA.debugLineNum = 123;BA.debugLine="stdGradient.AddState(stdGradient.State_Pressed,";
_stdgradient.AddState(_stdgradient.State_Pressed,(android.graphics.drawable.Drawable)(_gdwpressed.getObject()));
 //BA.debugLineNum = 124;BA.debugLine="stdGradient.AddState(stdGradient.State_Disabled";
_stdgradient.AddState(_stdgradient.State_Disabled,(android.graphics.drawable.Drawable)(_gdwdisabled.getObject()));
 //BA.debugLineNum = 125;BA.debugLine="Return stdGradient";
if (true) return _stdgradient;
 //BA.debugLineNum = 126;BA.debugLine="End Sub";
return null;
}
public static String  _dibujar_button(anywheresoftware.b4a.objects.ButtonWrapper _general,String _nombre,int _color_claro,int _color_obscuro,String _texto,int _left,int _top,int _largo,int _ancho) throws Exception{
 //BA.debugLineNum = 88;BA.debugLine="Sub dibujar_button(general As Button,nombre As Str";
 //BA.debugLineNum = 89;BA.debugLine="general.Initialize(nombre)";
_general.Initialize(mostCurrent.activityBA,_nombre);
 //BA.debugLineNum = 90;BA.debugLine="general.TextColor = Colors.White";
_general.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 91;BA.debugLine="general.Background = ButtonGradient(color_claro,c";
_general.setBackground((android.graphics.drawable.Drawable)(_buttongradient(_color_claro,_color_obscuro).getObject()));
 //BA.debugLineNum = 92;BA.debugLine="general.TextSize= 3%x";
_general.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (3),mostCurrent.activityBA)));
 //BA.debugLineNum = 93;BA.debugLine="general.Text = texto";
_general.setText((Object)(_texto));
 //BA.debugLineNum = 94;BA.debugLine="Activity.AddView(general, left, top, largo, an";
mostCurrent._activity.AddView((android.view.View)(_general.getObject()),_left,_top,_largo,_ancho);
 //BA.debugLineNum = 95;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_inicio() throws Exception{
 //BA.debugLineNum = 69;BA.debugLine="Sub dibujar_inicio";
 //BA.debugLineNum = 70;BA.debugLine="Activity.Title=\"Your Business\"";
mostCurrent._activity.setTitle((Object)("Your Business"));
 //BA.debugLineNum = 71;BA.debugLine="bmpLogo2.Gravity = Gravity.CENTER";
mostCurrent._bmplogo2.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 72;BA.debugLine="Activity.Background = bmpLogo2";
mostCurrent._activity.setBackground((android.graphics.drawable.Drawable)(mostCurrent._bmplogo2.getObject()));
 //BA.debugLineNum = 73;BA.debugLine="dibujar_label(lbl_pregunta,\"lbl_pregunta\",\"Tipo d";
_dibujar_label(mostCurrent._lbl_pregunta,"lbl_pregunta","Tipo de usuario:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 74;BA.debugLine="dibujar_button(btn_nuevo,\"btn_nuevo\",Colors.RGB(0";
_dibujar_button(mostCurrent._btn_nuevo,"btn_nuevo",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (76),(int) (153)),"Nuevo",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (35),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 75;BA.debugLine="dibujar_button(btn_registrado,\"btn_registrado\",Co";
_dibujar_button(mostCurrent._btn_registrado,"btn_registrado",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (73)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (102),(int) (51)),"Registrado",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 76;BA.debugLine="casos=1";
_casos = (int) (1);
 //BA.debugLineNum = 77;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_label(anywheresoftware.b4a.objects.LabelWrapper _general,String _nombre,String _texto,int _left,int _top,int _largo,int _ancho) throws Exception{
 //BA.debugLineNum = 79;BA.debugLine="Sub dibujar_label(general As Label,nombre As Strin";
 //BA.debugLineNum = 80;BA.debugLine="general.Initialize(nombre)";
_general.Initialize(mostCurrent.activityBA,_nombre);
 //BA.debugLineNum = 81;BA.debugLine="general.TextSize = 4%x";
_general.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (4),mostCurrent.activityBA)));
 //BA.debugLineNum = 82;BA.debugLine="general.TextColor = Colors.Black";
_general.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 83;BA.debugLine="general.Gravity = Gravity.CENTER";
_general.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 84;BA.debugLine="general.text = texto";
_general.setText((Object)(_texto));
 //BA.debugLineNum = 85;BA.debugLine="Activity.AddView(general,left,top,largo,ancho)";
mostCurrent._activity.AddView((android.view.View)(_general.getObject()),_left,_top,_largo,_ancho);
 //BA.debugLineNum = 86;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_splash() throws Exception{
int _killapp = 0;
 //BA.debugLineNum = 50;BA.debugLine="Sub dibujar_splash";
 //BA.debugLineNum = 51;BA.debugLine="lbl_splash.Initialize(\"lbl_splash\")";
mostCurrent._lbl_splash.Initialize(mostCurrent.activityBA,"lbl_splash");
 //BA.debugLineNum = 52;BA.debugLine="lbl_splash.TextSize = 3%x";
mostCurrent._lbl_splash.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (3),mostCurrent.activityBA)));
 //BA.debugLineNum = 53;BA.debugLine="lbl_splash.Background = bmpLogo";
mostCurrent._lbl_splash.setBackground((android.graphics.drawable.Drawable)(mostCurrent._bmplogo.getObject()));
 //BA.debugLineNum = 54;BA.debugLine="lbl_splash.Gravity = Gravity.CENTER";
mostCurrent._lbl_splash.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 55;BA.debugLine="Activity.AddView(lbl_splash,5%x,30%y,90%x,30%y";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._lbl_splash.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (30),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (90),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (30),mostCurrent.activityBA));
 //BA.debugLineNum = 56;BA.debugLine="Wait(4)";
_wait((int) (4));
 //BA.debugLineNum = 57;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 58;BA.debugLine="Msgbox2(\"Si en algún momento tiene una duda, pres";
anywheresoftware.b4a.keywords.Common.Msgbox2("Si en algún momento tiene una duda, presione el boton de ayuda en la parte superior.","Información","OK","","",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"informacion.png").getObject()),mostCurrent.activityBA);
 //BA.debugLineNum = 59;BA.debugLine="dibujar_inicio";
_dibujar_inicio();
 //BA.debugLineNum = 60;BA.debugLine="Dim KillApp As Int";
_killapp = 0;
 //BA.debugLineNum = 61;BA.debugLine="KillApp = Msgbox2(\"Esta aplicación requiere d";
_killapp = anywheresoftware.b4a.keywords.Common.Msgbox2("Esta aplicación requiere de un lector de códigos qr ¿Desea descargar uno?","Lector QR","Si","","No",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"logo_mensaje.png").getObject()),mostCurrent.activityBA);
 //BA.debugLineNum = 62;BA.debugLine="If KillApp = DialogResponse.POSITIVE Then";
if (_killapp==anywheresoftware.b4a.keywords.Common.DialogResponse.POSITIVE) { 
 //BA.debugLineNum = 63;BA.debugLine="StartActivity(intento)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(mostCurrent._intento.getObject()));
 }else {
 };
 //BA.debugLineNum = 67;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_text(anywheresoftware.b4a.objects.EditTextWrapper _general,String _nombre,int _tipo_entrada,String _texto,boolean _visible,int _left,int _top,int _largo,int _ancho) throws Exception{
 //BA.debugLineNum = 97;BA.debugLine="Sub dibujar_text(general As EditText,nombre As Str";
 //BA.debugLineNum = 98;BA.debugLine="general.Initialize(nombre)";
_general.Initialize(mostCurrent.activityBA,_nombre);
 //BA.debugLineNum = 99;BA.debugLine="general.TextSize = 2%x";
_general.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (2),mostCurrent.activityBA)));
 //BA.debugLineNum = 100;BA.debugLine="general.InputType=tipo_entrada";
_general.setInputType(_tipo_entrada);
 //BA.debugLineNum = 101;BA.debugLine="general.Hint = texto";
_general.setHint(_texto);
 //BA.debugLineNum = 102;BA.debugLine="general.PasswordMode= visible";
_general.setPasswordMode(_visible);
 //BA.debugLineNum = 103;BA.debugLine="Activity.AddView(general, left, top, largo, an";
mostCurrent._activity.AddView((android.view.View)(_general.getObject()),_left,_top,_largo,_ancho);
 //BA.debugLineNum = 104;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _globals() throws Exception{
 //BA.debugLineNum = 21;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 24;BA.debugLine="Dim casos As Int";
_casos = 0;
 //BA.debugLineNum = 25;BA.debugLine="Dim lbl_splash,lbl_bienvenido As Label";
mostCurrent._lbl_splash = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_bienvenido = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Dim bmpLogo  As BitmapDrawable";
mostCurrent._bmplogo = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
 //BA.debugLineNum = 27;BA.debugLine="Dim bmpLogo2  As BitmapDrawable";
mostCurrent._bmplogo2 = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
 //BA.debugLineNum = 28;BA.debugLine="Dim lbl_pregunta As Label";
mostCurrent._lbl_pregunta = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Dim btn_nuevo, btn_registrado, btn_general As But";
mostCurrent._btn_nuevo = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_registrado = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_general = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private intento As Intent";
mostCurrent._intento = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 31;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 19;BA.debugLine="End Sub";
return "";
}
public static String  _wait(int _sekunden) throws Exception{
long _ti = 0L;
 //BA.debugLineNum = 128;BA.debugLine="Sub Wait(Sekunden As Int)";
 //BA.debugLineNum = 129;BA.debugLine="Dim Ti As Long";
_ti = 0L;
 //BA.debugLineNum = 130;BA.debugLine="Ti = DateTime.now + (Sekunden * 1000)";
_ti = (long) (anywheresoftware.b4a.keywords.Common.DateTime.getNow()+(_sekunden*1000));
 //BA.debugLineNum = 131;BA.debugLine="Do While DateTime.now < Ti";
while (anywheresoftware.b4a.keywords.Common.DateTime.getNow()<_ti) {
 //BA.debugLineNum = 132;BA.debugLine="DoEvents";
anywheresoftware.b4a.keywords.Common.DoEvents();
 }
;
 //BA.debugLineNum = 134;BA.debugLine="End Sub";
return "";
}
}
