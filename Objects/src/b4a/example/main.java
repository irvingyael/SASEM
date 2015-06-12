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
public static b4a.example.main._renglones _renglon2 = null;
public static b4a.example.main._renglones _renglon = null;
public static b4a.example.main._renglones _renglon3 = null;
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
public anywheresoftware.b4a.objects.LabelWrapper _lbl_direccion_web = null;
public anywheresoftware.b4a.objects.EditTextWrapper _etxt_direccion_web = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_ingresar_url = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_complete = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_recuperar_pass = null;
public anywheresoftware.b4a.objects.EditTextWrapper _etxt_correo = null;
public anywheresoftware.b4a.objects.EditTextWrapper _etxt_pass = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_ingresar = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_hola = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_usuario = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_num_mascotas = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_tienes = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_mis_mascotas = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_historial = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_ultimos = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_mis_mascotas = null;
public anywheresoftware.b4a.objects.ListViewWrapper _lv_lista_mascotas = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imv_fotografia = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_nombre = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_raza = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_color = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_tamaño = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_fecha_adopcion = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_reportes = null;
public anywheresoftware.b4a.objects.ListViewWrapper _lv_lista_reportes = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imv_fotografia_reporte = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_escanear = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_tomar_foto = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_enviar = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_ultimos = null;
public anywheresoftware.b4a.objects.ListViewWrapper _lv_lista_ultimos = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _imv_fotografia_refugio = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_nombre_refugio = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_raza_refugio = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_color_refugio = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_tamaño_refugio = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_edad_aprox = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static class _renglones{
public boolean IsInitialized;
public String Renglon1;
public String Renglon2;
public void Initialize() {
IsInitialized = true;
Renglon1 = "";
Renglon2 = "";
}
@Override
		public String toString() {
			return BA.TypeToString(this, false);
		}}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 65;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 71;BA.debugLine="lv_lista_mascotas.Initialize(\"lv_lista_mascotas\")";
mostCurrent._lv_lista_mascotas.Initialize(mostCurrent.activityBA,"lv_lista_mascotas");
 //BA.debugLineNum = 72;BA.debugLine="lv_lista_reportes.Initialize(\"lv_lista_reportes\")";
mostCurrent._lv_lista_reportes.Initialize(mostCurrent.activityBA,"lv_lista_reportes");
 //BA.debugLineNum = 73;BA.debugLine="lv_lista_ultimos.Initialize(\"lv_lista_ultimos\")";
mostCurrent._lv_lista_ultimos.Initialize(mostCurrent.activityBA,"lv_lista_ultimos");
 //BA.debugLineNum = 74;BA.debugLine="imv_fotografia.Initialize(\"imv_fotografia\")";
mostCurrent._imv_fotografia.Initialize(mostCurrent.activityBA,"imv_fotografia");
 //BA.debugLineNum = 75;BA.debugLine="imv_fotografia_reporte.Initialize(\"imv_fotografia";
mostCurrent._imv_fotografia_reporte.Initialize(mostCurrent.activityBA,"imv_fotografia_reporte");
 //BA.debugLineNum = 76;BA.debugLine="imv_fotografia_refugio.Initialize(\"imv_fotografia";
mostCurrent._imv_fotografia_refugio.Initialize(mostCurrent.activityBA,"imv_fotografia_refugio");
 //BA.debugLineNum = 77;BA.debugLine="dibujar_configuracion";
_dibujar_configuracion();
 //BA.debugLineNum = 78;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 84;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 86;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 80;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 82;BA.debugLine="End Sub";
return "";
}
public static String  _btn_historial_click() throws Exception{
 //BA.debugLineNum = 155;BA.debugLine="Sub btn_historial_Click";
 //BA.debugLineNum = 156;BA.debugLine="dibujar_historial";
_dibujar_historial();
 //BA.debugLineNum = 157;BA.debugLine="End Sub";
return "";
}
public static String  _btn_ingresar_click() throws Exception{
 //BA.debugLineNum = 133;BA.debugLine="Sub btn_ingresar_Click";
 //BA.debugLineNum = 134;BA.debugLine="dibujar_datos";
_dibujar_datos();
 //BA.debugLineNum = 135;BA.debugLine="End Sub";
return "";
}
public static String  _btn_ingresar_url_click() throws Exception{
 //BA.debugLineNum = 114;BA.debugLine="Sub btn_ingresar_url_Click";
 //BA.debugLineNum = 115;BA.debugLine="dibujar_inicio";
_dibujar_inicio();
 //BA.debugLineNum = 116;BA.debugLine="End Sub";
return "";
}
public static String  _btn_mis_mascotas_click() throws Exception{
 //BA.debugLineNum = 151;BA.debugLine="Sub btn_mis_mascotas_Click";
 //BA.debugLineNum = 152;BA.debugLine="dibujar_mismascotas1";
_dibujar_mismascotas1();
 //BA.debugLineNum = 153;BA.debugLine="End Sub";
return "";
}
public static String  _btn_nuevo_click() throws Exception{
 //BA.debugLineNum = 159;BA.debugLine="Sub btn_nuevo_Click";
 //BA.debugLineNum = 160;BA.debugLine="dibujar_nuevoreporte";
_dibujar_nuevoreporte();
 //BA.debugLineNum = 161;BA.debugLine="End Sub";
return "";
}
public static String  _btn_ultimos_click() throws Exception{
 //BA.debugLineNum = 163;BA.debugLine="Sub btn_ultimos_Click";
 //BA.debugLineNum = 164;BA.debugLine="dibujar_ultimosagregados1";
_dibujar_ultimosagregados1();
 //BA.debugLineNum = 165;BA.debugLine="End Sub";
return "";
}
public static anywheresoftware.b4a.objects.drawable.StateListDrawable  _buttongradient(int _lightcolor,int _darkcolor) throws Exception{
anywheresoftware.b4a.agraham.reflection.Reflection _ref = null;
anywheresoftware.b4a.objects.drawable.ColorDrawable _gdwenabled = null;
anywheresoftware.b4a.objects.drawable.ColorDrawable _gdwpressed = null;
anywheresoftware.b4a.objects.drawable.ColorDrawable _gdwdisabled = null;
anywheresoftware.b4a.objects.drawable.StateListDrawable _stdgradient = null;
 //BA.debugLineNum = 289;BA.debugLine="Sub ButtonGradient(LightColor As Int, DarkColor As";
 //BA.debugLineNum = 290;BA.debugLine="Dim ref As Reflector";
_ref = new anywheresoftware.b4a.agraham.reflection.Reflection();
 //BA.debugLineNum = 292;BA.debugLine="Dim gdwEnabled As ColorDrawable";
_gdwenabled = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 293;BA.debugLine="gdwEnabled.Initialize(LightColor,15)";
_gdwenabled.Initialize(_lightcolor,(int) (15));
 //BA.debugLineNum = 295;BA.debugLine="Dim gdwPressed As ColorDrawable";
_gdwpressed = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 296;BA.debugLine="gdwPressed.Initialize(DarkColor,15)";
_gdwpressed.Initialize(_darkcolor,(int) (15));
 //BA.debugLineNum = 297;BA.debugLine="ref.Target = gdwPressed";
_ref.Target = (Object)(_gdwpressed.getObject());
 //BA.debugLineNum = 298;BA.debugLine="ref.RunMethod4(\"setCornerRadii\", Array As Objec";
_ref.RunMethod4("setCornerRadii",new Object[]{(Object)(new float[]{(float) (10),(float) (15),(float) (10),(float) (15),(float) (10),(float) (15),(float) (10),(float) (15)})},new String[]{"[F"});
 //BA.debugLineNum = 300;BA.debugLine="Dim gdwDisabled As ColorDrawable";
_gdwdisabled = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 301;BA.debugLine="gdwDisabled.Initialize(Colors.Gray,15)";
_gdwdisabled.Initialize(anywheresoftware.b4a.keywords.Common.Colors.Gray,(int) (15));
 //BA.debugLineNum = 303;BA.debugLine="Dim stdGradient As StateListDrawable";
_stdgradient = new anywheresoftware.b4a.objects.drawable.StateListDrawable();
 //BA.debugLineNum = 304;BA.debugLine="stdGradient.Initialize";
_stdgradient.Initialize();
 //BA.debugLineNum = 305;BA.debugLine="stdGradient.AddState2(Array As Int(stdGradient.";
_stdgradient.AddState2(new int[]{_stdgradient.State_Enabled,(int) (-_stdgradient.State_Pressed)},(android.graphics.drawable.Drawable)(_gdwenabled.getObject()));
 //BA.debugLineNum = 306;BA.debugLine="stdGradient.AddState(stdGradient.State_Pressed,";
_stdgradient.AddState(_stdgradient.State_Pressed,(android.graphics.drawable.Drawable)(_gdwpressed.getObject()));
 //BA.debugLineNum = 307;BA.debugLine="stdGradient.AddState(stdGradient.State_Disabled";
_stdgradient.AddState(_stdgradient.State_Disabled,(android.graphics.drawable.Drawable)(_gdwdisabled.getObject()));
 //BA.debugLineNum = 308;BA.debugLine="Return stdGradient";
if (true) return _stdgradient;
 //BA.debugLineNum = 309;BA.debugLine="End Sub";
return null;
}
public static String  _dibujar_button(anywheresoftware.b4a.objects.ButtonWrapper _general,String _nombre,int _color_claro,int _color_obscuro,String _texto,int _left,int _top,int _largo,int _ancho) throws Exception{
 //BA.debugLineNum = 271;BA.debugLine="Sub dibujar_button(general As Button,nombre As Str";
 //BA.debugLineNum = 272;BA.debugLine="general.Initialize(nombre)";
_general.Initialize(mostCurrent.activityBA,_nombre);
 //BA.debugLineNum = 273;BA.debugLine="general.TextColor = Colors.White";
_general.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 274;BA.debugLine="general.Background = ButtonGradient(color_claro,c";
_general.setBackground((android.graphics.drawable.Drawable)(_buttongradient(_color_claro,_color_obscuro).getObject()));
 //BA.debugLineNum = 275;BA.debugLine="general.TextSize= 3%x";
_general.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (3),mostCurrent.activityBA)));
 //BA.debugLineNum = 276;BA.debugLine="general.Text = texto";
_general.setText((Object)(_texto));
 //BA.debugLineNum = 277;BA.debugLine="Activity.AddView(general, left, top, largo, an";
mostCurrent._activity.AddView((android.view.View)(_general.getObject()),_left,_top,_largo,_ancho);
 //BA.debugLineNum = 278;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_configuracion() throws Exception{
 //BA.debugLineNum = 107;BA.debugLine="Sub dibujar_configuracion";
 //BA.debugLineNum = 108;BA.debugLine="Activity.Title=\"Seguimiento de Adopción\"";
mostCurrent._activity.setTitle((Object)("Seguimiento de Adopción"));
 //BA.debugLineNum = 109;BA.debugLine="dibujar_label(lbl_direccion_web,\"lbl_direccion_we";
_dibujar_label(mostCurrent._lbl_direccion_web,"lbl_direccion_web","Ingrese la direccion web del sitio de adopcion de su mascota:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA));
 //BA.debugLineNum = 110;BA.debugLine="dibujar_text(etxt_direccion_web,\"etxt_direccion_w";
_dibujar_text(mostCurrent._etxt_direccion_web,"etxt_direccion_web",mostCurrent._etxt_direccion_web.INPUT_TYPE_TEXT,"www.dejandohuella.com",anywheresoftware.b4a.keywords.Common.False,anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 111;BA.debugLine="dibujar_button(btn_ingresar_url,\"btn_ingresar_url";
_dibujar_button(mostCurrent._btn_ingresar_url,"btn_ingresar_url",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (76),(int) (153)),"Ingresar",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (35),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (30),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 112;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_datos() throws Exception{
 //BA.debugLineNum = 137;BA.debugLine="Sub dibujar_datos";
 //BA.debugLineNum = 138;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 139;BA.debugLine="Activity.Title=\"Datos personales\"";
mostCurrent._activity.setTitle((Object)("Datos personales"));
 //BA.debugLineNum = 140;BA.debugLine="dibujar_label(lbl_hola,\"lbl_hola\",\"Hola:\",0%x,2%y";
_dibujar_label(mostCurrent._lbl_hola,"lbl_hola","Hola:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (2),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 141;BA.debugLine="dibujar_label(lbl_usuario,\"lbl_usuario\",\"Irving A";
_dibujar_label(mostCurrent._lbl_usuario,"lbl_usuario","Irving Amador",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (30),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (2),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 142;BA.debugLine="dibujar_label(lbl_tienes,\"lbl_tienes\",\"Tienes:\",0";
_dibujar_label(mostCurrent._lbl_tienes,"lbl_tienes","Tienes:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 143;BA.debugLine="dibujar_label(lbl_num_mascotas,\"lbl_num_mascotas\"";
_dibujar_label(mostCurrent._lbl_num_mascotas,"lbl_num_mascotas","1 Mascota",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (30),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 144;BA.debugLine="dibujar_button(btn_mis_mascotas,\"btn_mis_mascotas";
_dibujar_button(mostCurrent._btn_mis_mascotas,"btn_mis_mascotas",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (76),(int) (153)),"Mis mascotas",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (23),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (99),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (22),mostCurrent.activityBA));
 //BA.debugLineNum = 145;BA.debugLine="dibujar_button(btn_historial,\"btn_historial\",Colo";
_dibujar_button(mostCurrent._btn_historial,"btn_historial",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (76),(int) (153)),"Historial de reportes",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (45.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (49),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA));
 //BA.debugLineNum = 146;BA.debugLine="dibujar_button(btn_nuevo,\"btn_nuevo\",Colors.RGB(0";
_dibujar_button(mostCurrent._btn_nuevo,"btn_nuevo",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (73)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (102),(int) (51)),"Nuevo reporte",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (45.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (49),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA));
 //BA.debugLineNum = 147;BA.debugLine="dibujar_button(btn_ultimos,\"btn_ultimos\",Colors.R";
_dibujar_button(mostCurrent._btn_ultimos,"btn_ultimos",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (73)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (102),(int) (51)),"Últimas mascotas agregadas",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0.5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (66),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (99),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (22),mostCurrent.activityBA));
 //BA.debugLineNum = 148;BA.debugLine="casos=1";
_casos = (int) (1);
 //BA.debugLineNum = 149;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_historial() throws Exception{
 //BA.debugLineNum = 202;BA.debugLine="Sub dibujar_historial";
 //BA.debugLineNum = 203;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 204;BA.debugLine="dibujar_label(lbl_reportes,\"lbl_reportes\",\"Mis Re";
_dibujar_label(mostCurrent._lbl_reportes,"lbl_reportes","Mis Reportes:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 205;BA.debugLine="lv_lista_reportes.Clear";
mostCurrent._lv_lista_reportes.Clear();
 //BA.debugLineNum = 206;BA.debugLine="Dim Renglon As Renglones";
_renglon = new b4a.example.main._renglones();
 //BA.debugLineNum = 207;BA.debugLine="Renglon.Renglon1 = \"Rocko\"";
_renglon.Renglon1 = "Rocko";
 //BA.debugLineNum = 208;BA.debugLine="Renglon.Renglon2 = \"19-05-15\"";
_renglon.Renglon2 = "19-05-15";
 //BA.debugLineNum = 209;BA.debugLine="lv_lista_reportes.TwoLinesLayout.Label.Color=C";
mostCurrent._lv_lista_reportes.getTwoLinesLayout().Label.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)));
 //BA.debugLineNum = 210;BA.debugLine="lv_lista_reportes.TwoLinesLayout.SecondLabel.C";
mostCurrent._lv_lista_reportes.getTwoLinesLayout().SecondLabel.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 211;BA.debugLine="lv_lista_reportes.AddTwoLines2(\"Reporte de: \"&";
mostCurrent._lv_lista_reportes.AddTwoLines2("Reporte de: "+_renglon.Renglon1,"Fecha: "+_renglon.Renglon2,(Object)(_renglon));
 //BA.debugLineNum = 212;BA.debugLine="lv_lista_reportes.Color=Colors.Transparent";
mostCurrent._lv_lista_reportes.setColor(anywheresoftware.b4a.keywords.Common.Colors.Transparent);
 //BA.debugLineNum = 213;BA.debugLine="Activity.AddView(lv_lista_reportes, 5%x, 20%y, 90";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._lv_lista_reportes.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (90),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (85),mostCurrent.activityBA));
 //BA.debugLineNum = 214;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_inicio() throws Exception{
 //BA.debugLineNum = 118;BA.debugLine="Sub dibujar_inicio";
 //BA.debugLineNum = 119;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 120;BA.debugLine="Activity.Title=\"Inicio de sesión\"";
mostCurrent._activity.setTitle((Object)("Inicio de sesión"));
 //BA.debugLineNum = 121;BA.debugLine="dibujar_label(lbl_complete,\"lbl_complete\",\"Ingres";
_dibujar_label(mostCurrent._lbl_complete,"lbl_complete","Ingrese sus datos:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 122;BA.debugLine="dibujar_text(etxt_correo,\"etxt_correo\",etxt_corre";
_dibujar_text(mostCurrent._etxt_correo,"etxt_correo",mostCurrent._etxt_correo.INPUT_TYPE_TEXT,"Correo",anywheresoftware.b4a.keywords.Common.False,anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (30),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 123;BA.debugLine="dibujar_text(etxt_pass,\"etxt_pass\",etxt_pass.INPU";
_dibujar_text(mostCurrent._etxt_pass,"etxt_pass",mostCurrent._etxt_pass.INPUT_TYPE_TEXT,"Contraseña",anywheresoftware.b4a.keywords.Common.True,anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (45),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 124;BA.debugLine="dibujar_button(btn_ingresar,\"btn_ingresar\",Colors";
_dibujar_button(mostCurrent._btn_ingresar,"btn_ingresar",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (76),(int) (153)),"Ingresar",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (35),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (30),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 125;BA.debugLine="lbl_recuperar_pass.Initialize(\"lbl_recuperar_pass";
mostCurrent._lbl_recuperar_pass.Initialize(mostCurrent.activityBA,"lbl_recuperar_pass");
 //BA.debugLineNum = 126;BA.debugLine="lbl_recuperar_pass.TextSize = 2%x";
mostCurrent._lbl_recuperar_pass.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (2),mostCurrent.activityBA)));
 //BA.debugLineNum = 127;BA.debugLine="lbl_recuperar_pass.TextColor = Colors.Gray";
mostCurrent._lbl_recuperar_pass.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 128;BA.debugLine="lbl_recuperar_pass.Gravity = Gravity.CENTER";
mostCurrent._lbl_recuperar_pass.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 129;BA.debugLine="lbl_recuperar_pass.text = \"He olvidado mi cont";
mostCurrent._lbl_recuperar_pass.setText((Object)("He olvidado mi contraseña"));
 //BA.debugLineNum = 130;BA.debugLine="Activity.AddView(lbl_recuperar_pass,0%x,60%y,1";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._lbl_recuperar_pass.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (60),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 131;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_label(anywheresoftware.b4a.objects.LabelWrapper _general,String _nombre,String _texto,int _left,int _top,int _largo,int _ancho) throws Exception{
 //BA.debugLineNum = 262;BA.debugLine="Sub dibujar_label(general As Label,nombre As Strin";
 //BA.debugLineNum = 263;BA.debugLine="general.Initialize(nombre)";
_general.Initialize(mostCurrent.activityBA,_nombre);
 //BA.debugLineNum = 264;BA.debugLine="general.TextSize = 3.5%x";
_general.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (3.5),mostCurrent.activityBA)));
 //BA.debugLineNum = 265;BA.debugLine="general.TextColor = Colors.Black";
_general.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 266;BA.debugLine="general.Gravity = Gravity.CENTER";
_general.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 267;BA.debugLine="general.text = texto";
_general.setText((Object)(_texto));
 //BA.debugLineNum = 268;BA.debugLine="Activity.AddView(general,left,top,largo,ancho)";
mostCurrent._activity.AddView((android.view.View)(_general.getObject()),_left,_top,_largo,_ancho);
 //BA.debugLineNum = 269;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_mismascotas1() throws Exception{
 //BA.debugLineNum = 167;BA.debugLine="Sub dibujar_mismascotas1";
 //BA.debugLineNum = 168;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 169;BA.debugLine="dibujar_label(lbl_mis_mascotas,\"lbl_mis_mascotas\"";
_dibujar_label(mostCurrent._lbl_mis_mascotas,"lbl_mis_mascotas","Mis Mascotas:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 170;BA.debugLine="lv_lista_mascotas.Clear";
mostCurrent._lv_lista_mascotas.Clear();
 //BA.debugLineNum = 171;BA.debugLine="Dim Renglon2 As Renglones";
_renglon2 = new b4a.example.main._renglones();
 //BA.debugLineNum = 172;BA.debugLine="Renglon2.Renglon1 = \"Rocko\"";
_renglon2.Renglon1 = "Rocko";
 //BA.debugLineNum = 173;BA.debugLine="Renglon2.Renglon2 = \"Mestizo\"";
_renglon2.Renglon2 = "Mestizo";
 //BA.debugLineNum = 174;BA.debugLine="lv_lista_mascotas.TwoLinesLayout.Label.Color=C";
mostCurrent._lv_lista_mascotas.getTwoLinesLayout().Label.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)));
 //BA.debugLineNum = 175;BA.debugLine="lv_lista_mascotas.TwoLinesLayout.SecondLabel.C";
mostCurrent._lv_lista_mascotas.getTwoLinesLayout().SecondLabel.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 176;BA.debugLine="lv_lista_mascotas.AddTwoLines2(\"Nombre: \"&Reng";
mostCurrent._lv_lista_mascotas.AddTwoLines2("Nombre: "+_renglon2.Renglon1,"Raza: "+_renglon2.Renglon2,(Object)(_renglon2));
 //BA.debugLineNum = 177;BA.debugLine="lv_lista_mascotas.Color=Colors.Transparent";
mostCurrent._lv_lista_mascotas.setColor(anywheresoftware.b4a.keywords.Common.Colors.Transparent);
 //BA.debugLineNum = 178;BA.debugLine="Activity.AddView(lv_lista_mascotas, 5%x, 20%y, 90";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._lv_lista_mascotas.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (90),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (85),mostCurrent.activityBA));
 //BA.debugLineNum = 179;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_mismascotas2() throws Exception{
 //BA.debugLineNum = 190;BA.debugLine="Sub dibujar_mismascotas2";
 //BA.debugLineNum = 191;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 192;BA.debugLine="Activity.Title=\"Datos de mi mascota\"";
mostCurrent._activity.setTitle((Object)("Datos de mi mascota"));
 //BA.debugLineNum = 193;BA.debugLine="imv_fotografia.Color=Colors.Gray";
mostCurrent._imv_fotografia.setColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 194;BA.debugLine="Activity.AddView(imv_fotografia,25%x, 5%y, 50%x,";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._imv_fotografia.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (25),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (35),mostCurrent.activityBA));
 //BA.debugLineNum = 195;BA.debugLine="dibujar_label(lbl_nombre,\"lbl_nombre\",\"Nombre:\",0";
_dibujar_label(mostCurrent._lbl_nombre,"lbl_nombre","Nombre:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 196;BA.debugLine="dibujar_label(lbl_raza,\"lbl_raza\",\"Raza:\",0%x,50%";
_dibujar_label(mostCurrent._lbl_raza,"lbl_raza","Raza:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 197;BA.debugLine="dibujar_label(lbl_color,\"lbl_color\",\"Color:\",0%x,";
_dibujar_label(mostCurrent._lbl_color,"lbl_color","Color:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (60),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 198;BA.debugLine="dibujar_label(lbl_tamaño,\"lbl_tamaño\",\"Tamaño:\",0";
_dibujar_label(mostCurrent._lbl_tamaño,"lbl_tamaño","Tamaño:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 199;BA.debugLine="dibujar_label(lbl_fecha_adopcion,\"lbl_fecha_adopc";
_dibujar_label(mostCurrent._lbl_fecha_adopcion,"lbl_fecha_adopcion","Fecha de Adopción:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (80),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 200;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_nuevoreporte() throws Exception{
 //BA.debugLineNum = 216;BA.debugLine="Sub dibujar_nuevoreporte";
 //BA.debugLineNum = 217;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 218;BA.debugLine="Activity.Title=\"Nuevo Reporte\"";
mostCurrent._activity.setTitle((Object)("Nuevo Reporte"));
 //BA.debugLineNum = 219;BA.debugLine="imv_fotografia_reporte.Color=Colors.Gray";
mostCurrent._imv_fotografia_reporte.setColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 220;BA.debugLine="Activity.AddView(imv_fotografia_reporte,15%x, 5%y";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._imv_fotografia_reporte.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (50),mostCurrent.activityBA));
 //BA.debugLineNum = 221;BA.debugLine="dibujar_button(btn_escanear,\"btn_escanear\",Colors";
_dibujar_button(mostCurrent._btn_escanear,"btn_escanear",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (76),(int) (153)),"Escanear QR",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (60),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 222;BA.debugLine="dibujar_button(btn_tomar_foto,\"btn_tomar_foto\",Co";
_dibujar_button(mostCurrent._btn_tomar_foto,"btn_tomar_foto",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (76),(int) (153)),"Tomar Fotografia",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (15),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (72),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 223;BA.debugLine="dibujar_button(btn_enviar,\"btn_enviar\",Colors.RGB";
_dibujar_button(mostCurrent._btn_enviar,"btn_enviar",anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)),anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (76),(int) (153)),"Enviar",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (25),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (84),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 224;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_splash() throws Exception{
int _killapp = 0;
 //BA.debugLineNum = 88;BA.debugLine="Sub dibujar_splash";
 //BA.debugLineNum = 89;BA.debugLine="lbl_splash.Initialize(\"lbl_splash\")";
mostCurrent._lbl_splash.Initialize(mostCurrent.activityBA,"lbl_splash");
 //BA.debugLineNum = 90;BA.debugLine="lbl_splash.TextSize = 3%x";
mostCurrent._lbl_splash.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (3),mostCurrent.activityBA)));
 //BA.debugLineNum = 91;BA.debugLine="lbl_splash.Background = bmpLogo";
mostCurrent._lbl_splash.setBackground((android.graphics.drawable.Drawable)(mostCurrent._bmplogo.getObject()));
 //BA.debugLineNum = 92;BA.debugLine="lbl_splash.Gravity = Gravity.CENTER";
mostCurrent._lbl_splash.setGravity(anywheresoftware.b4a.keywords.Common.Gravity.CENTER);
 //BA.debugLineNum = 93;BA.debugLine="Activity.AddView(lbl_splash,5%x,30%y,90%x,30%y";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._lbl_splash.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (30),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (90),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (30),mostCurrent.activityBA));
 //BA.debugLineNum = 94;BA.debugLine="Wait(4)";
_wait((int) (4));
 //BA.debugLineNum = 95;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 96;BA.debugLine="Msgbox2(\"Si en algún momento tiene una duda, pres";
anywheresoftware.b4a.keywords.Common.Msgbox2("Si en algún momento tiene una duda, presione el boton de ayuda en la parte superior.","Información","OK","","",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"informacion.png").getObject()),mostCurrent.activityBA);
 //BA.debugLineNum = 97;BA.debugLine="dibujar_inicio";
_dibujar_inicio();
 //BA.debugLineNum = 98;BA.debugLine="Dim KillApp As Int";
_killapp = 0;
 //BA.debugLineNum = 99;BA.debugLine="KillApp = Msgbox2(\"Esta aplicación requiere d";
_killapp = anywheresoftware.b4a.keywords.Common.Msgbox2("Esta aplicación requiere de un lector de códigos qr ¿Desea descargar uno?","Lector QR","Si","","No",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"logo_mensaje.png").getObject()),mostCurrent.activityBA);
 //BA.debugLineNum = 100;BA.debugLine="If KillApp = DialogResponse.POSITIVE Then";
if (_killapp==anywheresoftware.b4a.keywords.Common.DialogResponse.POSITIVE) { 
 //BA.debugLineNum = 101;BA.debugLine="StartActivity(intento)";
anywheresoftware.b4a.keywords.Common.StartActivity(mostCurrent.activityBA,(Object)(mostCurrent._intento.getObject()));
 }else {
 };
 //BA.debugLineNum = 105;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_text(anywheresoftware.b4a.objects.EditTextWrapper _general,String _nombre,int _tipo_entrada,String _texto,boolean _visible,int _left,int _top,int _largo,int _ancho) throws Exception{
 //BA.debugLineNum = 280;BA.debugLine="Sub dibujar_text(general As EditText,nombre As Str";
 //BA.debugLineNum = 281;BA.debugLine="general.Initialize(nombre)";
_general.Initialize(mostCurrent.activityBA,_nombre);
 //BA.debugLineNum = 282;BA.debugLine="general.TextSize = 2%x";
_general.setTextSize((float) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (2),mostCurrent.activityBA)));
 //BA.debugLineNum = 283;BA.debugLine="general.InputType=tipo_entrada";
_general.setInputType(_tipo_entrada);
 //BA.debugLineNum = 284;BA.debugLine="general.Hint = texto";
_general.setHint(_texto);
 //BA.debugLineNum = 285;BA.debugLine="general.PasswordMode= visible";
_general.setPasswordMode(_visible);
 //BA.debugLineNum = 286;BA.debugLine="Activity.AddView(general, left, top, largo, an";
mostCurrent._activity.AddView((android.view.View)(_general.getObject()),_left,_top,_largo,_ancho);
 //BA.debugLineNum = 287;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_ultimosagregados1() throws Exception{
 //BA.debugLineNum = 226;BA.debugLine="Sub dibujar_ultimosagregados1";
 //BA.debugLineNum = 227;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 228;BA.debugLine="Activity.Title=(\"Mascotas disponibles\")";
mostCurrent._activity.setTitle((Object)(("Mascotas disponibles")));
 //BA.debugLineNum = 229;BA.debugLine="dibujar_label(lbl_ultimos,\"lbl_ultimos\",\"Mascotas";
_dibujar_label(mostCurrent._lbl_ultimos,"lbl_ultimos","Mascotas nuevas para adoptar:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 230;BA.debugLine="lv_lista_ultimos.Clear";
mostCurrent._lv_lista_ultimos.Clear();
 //BA.debugLineNum = 231;BA.debugLine="Dim Renglon3 As Renglones";
_renglon3 = new b4a.example.main._renglones();
 //BA.debugLineNum = 232;BA.debugLine="Renglon3.Renglon1 = \"Max\"";
_renglon3.Renglon1 = "Max";
 //BA.debugLineNum = 233;BA.debugLine="Renglon3.Renglon2 = \"Pug\"";
_renglon3.Renglon2 = "Pug";
 //BA.debugLineNum = 234;BA.debugLine="lv_lista_ultimos.TwoLinesLayout.Label.Color=Co";
mostCurrent._lv_lista_ultimos.getTwoLinesLayout().Label.setColor(anywheresoftware.b4a.keywords.Common.Colors.RGB((int) (0),(int) (153),(int) (153)));
 //BA.debugLineNum = 235;BA.debugLine="lv_lista_ultimos.TwoLinesLayout.SecondLabel.Co";
mostCurrent._lv_lista_ultimos.getTwoLinesLayout().SecondLabel.setColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 236;BA.debugLine="lv_lista_ultimos.AddTwoLines2(\"Nombre: \"&Rengl";
mostCurrent._lv_lista_ultimos.AddTwoLines2("Nombre: "+_renglon3.Renglon1,"Raza: "+_renglon3.Renglon2,(Object)(_renglon3));
 //BA.debugLineNum = 237;BA.debugLine="lv_lista_ultimos.Color=Colors.Transparent";
mostCurrent._lv_lista_ultimos.setColor(anywheresoftware.b4a.keywords.Common.Colors.Transparent);
 //BA.debugLineNum = 238;BA.debugLine="Activity.AddView(lv_lista_ultimos, 5%x, 20%y, 90%";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._lv_lista_ultimos.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (20),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (90),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (85),mostCurrent.activityBA));
 //BA.debugLineNum = 239;BA.debugLine="End Sub";
return "";
}
public static String  _dibujar_ultimosagregados2() throws Exception{
 //BA.debugLineNum = 250;BA.debugLine="Sub dibujar_ultimosagregados2";
 //BA.debugLineNum = 251;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 252;BA.debugLine="Activity.Title=\"Datos de la mascota\"";
mostCurrent._activity.setTitle((Object)("Datos de la mascota"));
 //BA.debugLineNum = 253;BA.debugLine="imv_fotografia_refugio.Color=Colors.Gray";
mostCurrent._imv_fotografia_refugio.setColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 254;BA.debugLine="Activity.AddView(imv_fotografia_refugio,25%x, 5%y";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._imv_fotografia_refugio.getObject()),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (25),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (5),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (35),mostCurrent.activityBA));
 //BA.debugLineNum = 255;BA.debugLine="dibujar_label(lbl_nombre_refugio,\"lbl_nombre_refu";
_dibujar_label(mostCurrent._lbl_nombre_refugio,"lbl_nombre_refugio","Nombre:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 256;BA.debugLine="dibujar_label(lbl_raza_refugio,\"lbl_raza_refugio\"";
_dibujar_label(mostCurrent._lbl_raza_refugio,"lbl_raza_refugio","Raza:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (50),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 257;BA.debugLine="dibujar_label(lbl_color_refugio,\"lbl_color_refugi";
_dibujar_label(mostCurrent._lbl_color_refugio,"lbl_color_refugio","Color:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (60),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 258;BA.debugLine="dibujar_label(lbl_tamaño_refugio,\"lbl_tamaño_refu";
_dibujar_label(mostCurrent._lbl_tamaño_refugio,"lbl_tamaño_refugio","Tamaño:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (40),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 259;BA.debugLine="dibujar_label(lbl_edad_aprox,\"lbl_edad_aprox\",\"Ed";
_dibujar_label(mostCurrent._lbl_edad_aprox,"lbl_edad_aprox","Edad Aproximada:",anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (0),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (80),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (70),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (10),mostCurrent.activityBA));
 //BA.debugLineNum = 260;BA.debugLine="End Sub";
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
 //BA.debugLineNum = 23;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 26;BA.debugLine="Dim casos As Int";
_casos = 0;
 //BA.debugLineNum = 27;BA.debugLine="Dim lbl_splash,lbl_bienvenido As Label";
mostCurrent._lbl_splash = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_bienvenido = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Dim bmpLogo  As BitmapDrawable";
mostCurrent._bmplogo = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
 //BA.debugLineNum = 29;BA.debugLine="Dim bmpLogo2  As BitmapDrawable";
mostCurrent._bmplogo2 = new anywheresoftware.b4a.objects.drawable.BitmapDrawable();
 //BA.debugLineNum = 30;BA.debugLine="Dim lbl_pregunta As Label";
mostCurrent._lbl_pregunta = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Dim btn_nuevo, btn_registrado, btn_general As But";
mostCurrent._btn_nuevo = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_registrado = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_general = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private intento As Intent";
mostCurrent._intento = new anywheresoftware.b4a.objects.IntentWrapper();
 //BA.debugLineNum = 34;BA.debugLine="Dim lbl_direccion_web As Label";
mostCurrent._lbl_direccion_web = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 35;BA.debugLine="Dim etxt_direccion_web As EditText";
mostCurrent._etxt_direccion_web = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 36;BA.debugLine="Dim btn_ingresar_url As Button";
mostCurrent._btn_ingresar_url = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 38;BA.debugLine="Dim lbl_complete, lbl_recuperar_pass As Label";
mostCurrent._lbl_complete = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_recuperar_pass = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 39;BA.debugLine="Dim etxt_correo, etxt_pass As EditText";
mostCurrent._etxt_correo = new anywheresoftware.b4a.objects.EditTextWrapper();
mostCurrent._etxt_pass = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Dim btn_ingresar As Button";
mostCurrent._btn_ingresar = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 42;BA.debugLine="Dim lbl_hola,lbl_usuario, lbl_num_mascotas, lbl_t";
mostCurrent._lbl_hola = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_usuario = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_num_mascotas = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_tienes = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 43;BA.debugLine="Dim btn_mis_mascotas, btn_historial, btn_nuevo, b";
mostCurrent._btn_mis_mascotas = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_historial = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_nuevo = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_ultimos = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 45;BA.debugLine="Dim lbl_mis_mascotas As Label";
mostCurrent._lbl_mis_mascotas = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 46;BA.debugLine="Dim lv_lista_mascotas As ListView";
mostCurrent._lv_lista_mascotas = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 47;BA.debugLine="Type Renglones (Renglon1 As String, Renglon2 As S";
;
 //BA.debugLineNum = 49;BA.debugLine="Dim imv_fotografia As ImageView";
mostCurrent._imv_fotografia = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 50;BA.debugLine="Dim lbl_nombre, lbl_raza, lbl_color, lbl_tamaño,";
mostCurrent._lbl_nombre = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_raza = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_color = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_tamaño = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_fecha_adopcion = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 52;BA.debugLine="Dim lbl_reportes As Label";
mostCurrent._lbl_reportes = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 53;BA.debugLine="Dim lv_lista_reportes As ListView";
mostCurrent._lv_lista_reportes = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 55;BA.debugLine="Dim imv_fotografia_reporte As ImageView";
mostCurrent._imv_fotografia_reporte = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 56;BA.debugLine="Dim btn_escanear, btn_tomar_foto, btn_enviar As B";
mostCurrent._btn_escanear = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_tomar_foto = new anywheresoftware.b4a.objects.ButtonWrapper();
mostCurrent._btn_enviar = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 58;BA.debugLine="Dim lbl_ultimos As Label";
mostCurrent._lbl_ultimos = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 59;BA.debugLine="Dim lv_lista_ultimos As ListView";
mostCurrent._lv_lista_ultimos = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 61;BA.debugLine="Dim imv_fotografia_refugio As ImageView";
mostCurrent._imv_fotografia_refugio = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 62;BA.debugLine="Dim lbl_nombre_refugio, lbl_raza_refugio, lbl_col";
mostCurrent._lbl_nombre_refugio = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_raza_refugio = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_color_refugio = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_tamaño_refugio = new anywheresoftware.b4a.objects.LabelWrapper();
mostCurrent._lbl_edad_aprox = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 63;BA.debugLine="End Sub";
return "";
}
public static String  _lv_lista_mascotas_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 181;BA.debugLine="Sub lv_lista_mascotas_ItemClick (Position As Int,";
 //BA.debugLineNum = 187;BA.debugLine="dibujar_mismascotas2";
_dibujar_mismascotas2();
 //BA.debugLineNum = 188;BA.debugLine="End Sub";
return "";
}
public static String  _lv_lista_ultimos_itemclick(int _position,Object _value) throws Exception{
 //BA.debugLineNum = 241;BA.debugLine="Sub lv_lista_ultimos_ItemClick (Position As Int, V";
 //BA.debugLineNum = 247;BA.debugLine="dibujar_ultimosagregados2";
_dibujar_ultimosagregados2();
 //BA.debugLineNum = 248;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 18;BA.debugLine="Dim Renglon2 As Renglones";
_renglon2 = new b4a.example.main._renglones();
 //BA.debugLineNum = 19;BA.debugLine="Dim Renglon As Renglones";
_renglon = new b4a.example.main._renglones();
 //BA.debugLineNum = 20;BA.debugLine="Dim Renglon3 As Renglones";
_renglon3 = new b4a.example.main._renglones();
 //BA.debugLineNum = 21;BA.debugLine="End Sub";
return "";
}
public static String  _wait(int _sekunden) throws Exception{
long _ti = 0L;
 //BA.debugLineNum = 311;BA.debugLine="Sub Wait(Sekunden As Int)";
 //BA.debugLineNum = 312;BA.debugLine="Dim Ti As Long";
_ti = 0L;
 //BA.debugLineNum = 313;BA.debugLine="Ti = DateTime.now + (Sekunden * 1000)";
_ti = (long) (anywheresoftware.b4a.keywords.Common.DateTime.getNow()+(_sekunden*1000));
 //BA.debugLineNum = 314;BA.debugLine="Do While DateTime.now < Ti";
while (anywheresoftware.b4a.keywords.Common.DateTime.getNow()<_ti) {
 //BA.debugLineNum = 315;BA.debugLine="DoEvents";
anywheresoftware.b4a.keywords.Common.DoEvents();
 }
;
 //BA.debugLineNum = 317;BA.debugLine="End Sub";
return "";
}
}
