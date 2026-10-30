package org.netbeans.lib.awtextra;
import java.awt.*;
import java.util.*;
/** Small source-compatible AbsoluteLayout implementation for NetBeans-generated Swing forms. */
public class AbsoluteLayout implements LayoutManager2, java.io.Serializable {
    private final Map<Component, AbsoluteConstraints> constraints = new LinkedHashMap<>();
    public void addLayoutComponent(String name, Component comp) { constraints.put(comp,new AbsoluteConstraints(0,0)); }
    public void addLayoutComponent(Component comp,Object c) { constraints.put(comp,c instanceof AbsoluteConstraints?(AbsoluteConstraints)c:new AbsoluteConstraints(0,0)); }
    public void removeLayoutComponent(Component comp) { constraints.remove(comp); }
    public Dimension preferredLayoutSize(Container parent) { synchronized(parent.getTreeLock()){ Insets i=parent.getInsets(); int w=0,h=0; for(Map.Entry<Component,AbsoluteConstraints> e:constraints.entrySet()){Component c=e.getKey(); if(!c.isVisible())continue; AbsoluteConstraints a=e.getValue(); Dimension d=c.getPreferredSize(); w=Math.max(w,a.x+(a.width>=0?a.width:d.width)); h=Math.max(h,a.y+(a.height>=0?a.height:d.height)); } return new Dimension(w+i.left+i.right,h+i.top+i.bottom); } }
    public Dimension minimumLayoutSize(Container parent) { return preferredLayoutSize(parent); }
    public Dimension maximumLayoutSize(Container target) { return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE); }
    public void layoutContainer(Container parent) { synchronized(parent.getTreeLock()){ Insets i=parent.getInsets(); for(Map.Entry<Component,AbsoluteConstraints> e:constraints.entrySet()){Component c=e.getKey(); if(!c.isVisible())continue; AbsoluteConstraints a=e.getValue(); Dimension d=c.getPreferredSize(); c.setBounds(i.left+a.x,i.top+a.y,a.width>=0?a.width:d.width,a.height>=0?a.height:d.height); } } }
    public float getLayoutAlignmentX(Container target){return 0f;} public float getLayoutAlignmentY(Container target){return 0f;} public void invalidateLayout(Container target){}
}
