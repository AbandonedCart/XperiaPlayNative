/*
 * FS.java
 * Copyright (C) 2003
 * 
 * $Id: FS.java,v 1.15 2005/11/13 13:36:00 cawe Exp $
 */
/*
 Copyright (C) 1997-2001 Id Software, Inc.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */
package quake.two.android;


//import jake2.game.cvar_t;


import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * FS
 * 
 * @author cwei
 */
public /*final*/ class FS /*extends Globals*/ {

    /*
     * ==================================================
     * 
     * QUAKE FILESYSTEM
     * 
     * ==================================================
     */

    public static class packfile_t {
        static final int SIZE = 64;

        static final int NAME_SIZE = 56;

        public String name; // char name[56]

        public int filepos, filelen;

        public String toString() {
            return name + " [ length: " + filelen + " pos: " + filepos + " ]";
        }
    }

    public static class pack_t {
        String filename;

        RandomAccessFile handle;
        
        ByteBuffer backbuffer;

        int numfiles;

        public Hashtable files; // with packfile_t entries
    }

    public static String fs_gamedir;

    private static String fs_userdir;

   /* public static cvar_t fs_basedir;

    public static cvar_t fs_cddir;

    public static cvar_t fs_gamedirvar;
*/
    
    public static class filelink_t {
        String from;

        int fromlength;

        String to;
    }

    // with filelink_t entries
    public static List fs_links = new LinkedList();

    public static class searchpath_t {
        String filename;

        pack_t pack; // only one of filename or pack will be used

        searchpath_t next;
    }

    public static searchpath_t fs_searchpaths;

    // without gamedirs
    public static searchpath_t fs_base_searchpaths;

    /*
     * All of Quake's data access is through a hierchal file system, but the
     * contents of the file system can be transparently merged from several
     * sources.
     * 
     * The "base directory" is the path to the directory holding the quake.exe
     * and all game directories. The sys_* files pass this to host_init in
     * quakeparms_t->basedir. This can be overridden with the "-basedir" command
     * line parm to allow code debugging in a different directory. The base
     * directory is only used during filesystem initialization.
     * 
     * The "game directory" is the first tree on the search path and directory
     * that all generated files (savegames, screenshots, demos, config files)
     * will be saved to. This can be overridden with the "-game" command line
     * parameter. The game directory can never be changed while quake is
     * executing. This is a precacution against having a malicious server
     * instruct clients to write files over areas they shouldn't.
     *  
     */




    static final int IDPAKHEADER = (('K' << 24) + ('C' << 16) + ('A' << 8) + 'P');

    static class dpackheader_t {
        int ident; // IDPAKHEADER

        int dirofs;

        int dirlen;
    }

    static final int MAX_FILES_IN_PACK = 4096;

    // buffer for C-Strings char[56]
    static byte[] tmpText = new byte[packfile_t.NAME_SIZE];

    /*
     * LoadPackFile
     * 
     * Takes an explicit (not game tree related) path to a pak file.
     * 
     * Loads the header and directory, adding the files at the beginning of the
     * list so they override previous pack files.
     */
    
    static class PackHandle {
    	

    	private RandomAccessFile file;
    	
    	public PackHandle(RandomAccessFile file) throws FileNotFoundException {   
   
				
			this.file = file;
		
		
    	}

		public long limit() throws IOException {
			//return new File(filename).length();
			return file.length();
		}
		
		//ByteOrder.LITTLE_ENDIAN
		public int getInt() throws IOException {
			int a = file.read();
			int b = file.read();
			int c = file.read();
			int d = file.read();
			return (a) | (b<<8) | (c<<16) | (d<<24);
		}

		public void position(long n) throws IOException {
			file.seek(n);
			
		}

		public void get(byte[] b)  throws IOException {
			file.read(b);			
		}
    	
    	
    }
    
    public static pack_t LoadPackFile(String packfile) {

        dpackheader_t header;
        Hashtable newfiles;
        RandomAccessFile file;
        int numpackfiles = 0;
        pack_t pack = null;
        //		unsigned checksum;
        //
        try {
        	file = new RandomAccessFile(packfile, "r");
        	/*FileChannel fc = file.getChannel();
            ByteBuffer packhandle = fc.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            packhandle.order(ByteOrder.LITTLE_ENDIAN);
 
            fc.close();
            */
        	PackHandle packhandle = new PackHandle(file);
            
            if (packhandle == null || packhandle.limit() < 1)
                return null;
            //
            header = new dpackheader_t();
            header.ident = packhandle.getInt();
            header.dirofs = packhandle.getInt();
            header.dirlen = packhandle.getInt();

            if (header.ident != IDPAKHEADER)
                throw new Error(packfile + " is not a packfile");

            numpackfiles = header.dirlen / packfile_t.SIZE;

            if (numpackfiles > MAX_FILES_IN_PACK)
            	throw new Error(packfile + " has " + numpackfiles
                        + " files");

            newfiles = new Hashtable(numpackfiles);

            packhandle.position(header.dirofs);

            // parse the directory
            packfile_t entry = null;

            for (int i = 0; i < numpackfiles; i++) {
                packhandle.get(tmpText);

                entry = new packfile_t();
                entry.name = new String(tmpText).trim();
                entry.filepos = packhandle.getInt();
                entry.filelen = packhandle.getInt();

                //System.out.println("adding packfile : " + entry.name.toLowerCase());
                newfiles.put(entry.name.toLowerCase(), entry);
            }

        } catch (IOException e) {
        	 System.out.print(e.getMessage() + '\n');
            return null;
        }

        pack = new pack_t();
        pack.filename = new String(packfile);
        pack.handle = file;
        pack.numfiles = numpackfiles;
        pack.files = newfiles;

        System.out.print("Added packfile " + packfile + " (" + numpackfiles
                + " files)\n");

        return pack;
    }

}
