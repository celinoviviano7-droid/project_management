package gestion.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Toast {
    private static Frame owner;
    private static final List<Widgets.Toast> active = new ArrayList<>();

    public static void init(Frame f) { owner=f; }

    public static void ok(String msg)   { show(msg, Widgets.ToastType.SUCCESS); }
    public static void info(String msg) { show(msg, Widgets.ToastType.INFO);    }
    public static void warn(String msg) { show(msg, Widgets.ToastType.WARNING); }
    public static void err(String msg)  { show(msg, Widgets.ToastType.ERROR);   }

    private static void show(String msg, Widgets.ToastType t){
        if(owner==null) return;
        SwingUtilities.invokeLater(()->{
            active.removeIf(w -> !w.isVisible());
            int off = active.size() * 58;
            Widgets.Toast w = new Widgets.Toast(owner, msg, t);
            active.add(w);
            w.showToast(owner, off);
        });
    }
}
