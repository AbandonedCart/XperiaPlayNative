package org.zeus.arena;

import java.util.List;
import org.zeus.arena.R;

public class ContactPoint {
	
	public Point coordinates;
	public List<Control> controls;
	
	public ContactPoint(Point coordinates, List<Control> controls){
		this.coordinates = coordinates;
		this.controls = controls;
	}

}
