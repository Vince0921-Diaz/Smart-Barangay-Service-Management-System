package org.netbeans.lib.awtextra;
import java.awt.Dimension;
/** Source-compatible replacement for NetBeans AbsoluteConstraints. */
public class AbsoluteConstraints {
    public final int x, y, width, height;
    public AbsoluteConstraints(int x, int y) { this(x,y,-1,-1); }
    public AbsoluteConstraints(int x, int y, int width, int height) { this.x=x; this.y=y; this.width=width; this.height=height; }
    public AbsoluteConstraints(int x, int y, Dimension size) { this(x,y,size==null?-1:size.width,size==null?-1:size.height); }
}
