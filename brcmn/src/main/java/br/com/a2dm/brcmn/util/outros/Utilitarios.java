package br.com.a2dm.brcmn.util.outros;

import java.io.File;

public class Utilitarios
{
	public static void removerArquivos(String dir)
	{
		File file = new File(dir);
		
		if (file.isDirectory())
		{
            File[] files = file.listFiles();
            
            for (File f : files)
            {
                    f.delete();
            }
		}
	}
}
