package senti.utilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileOps {
   /**
    * 该方法备份文件并删除原始文件。对备份文件进行递归查找，找到第一个存在的备份文件并删除，然后对所有备份文件进行后移操作。
    * @param sFileName 文件名
    * @param iMaxBackups 最大备份次数
    * @return 找到就是true，否则就是false
    */
   public static boolean backupFileAndDeleteOriginal(String sFileName, int iMaxBackups) {
      int iLastBackup;
      File f;
      for(iLastBackup = iMaxBackups; iLastBackup >= 0; --iLastBackup) {
         f = new File(sFileName + iLastBackup + ".bak");
         if (f.exists()) {
            break;
         }
      }

      if (iLastBackup < 1) {
         f = new File(sFileName);
         if (f.exists()) {
            f.renameTo(new File(sFileName + "1.bak"));
            return true;
         } else {
            return false;
         }
      } else {
         if (iLastBackup == iMaxBackups) {
            f = new File(sFileName + iLastBackup + ".bak");
            f.delete();
            --iLastBackup;
         }

         for(int i = iLastBackup; i > 0; --i) {
            f = new File(sFileName + i + ".bak");
            f.renameTo(new File(sFileName + (i + 1) + ".bak"));
         }

         f = new File(sFileName);
         f.renameTo(new File(sFileName + "1.bak"));
         return true;
      }
   }

   /**
    * 使用BufferedReader读取文件，并在每个换行符处增加一个计数器。最后，它返回计数器的值。
    * @param sFileLocation 文件路径
    * @return 计数器的值
    */
   public static int i_CountLinesInTextFile(String sFileLocation) {
      int iLines = 0;

      try {
         BufferedReader rReader;
         for(rReader = new BufferedReader(new FileReader(sFileLocation)); rReader.ready(); ++iLines) {
            String sLine = rReader.readLine();
         }

         rReader.close();
         return iLines;
      } catch (FileNotFoundException var5) {
         var5.printStackTrace();
         return -1;
      } catch (IOException var6) {
         var6.printStackTrace();
         return -1;
      }
   }

   /**
    * 获取下一个可用的文件名，将文件名前缀、数字和文件名后缀拼接在一起以形成新的文件名，并检查该文件名是否已存在。
    * @param sFileNameStart  前缀
    * @param sFileNameEnd 后缀
    * @return 生成的文件名
    */
   public static String getNextAvailableFilename(String sFileNameStart, String sFileNameEnd) {
      for(int i = 0; i <= 1000; ++i) {
         String sFileName = sFileNameStart + i + sFileNameEnd;
         File f = new File(sFileName);
         if (!f.isFile()) {
            return sFileName;
         }
      }

      return "";
   }

   /**
    * 用于删除文件名中的扩展名，文件名不为空且包含扩展名，则返回不包含扩展名的文件名。否则，返回原始文件名。
    * @param sFilename 文件名
    * @return 文件名
    */
   public static String s_ChopFileNameExtension(String sFilename) {
      if (sFilename != null && sFilename != "") {
         int iLastDotPos = sFilename.lastIndexOf(".");
         if (iLastDotPos > 0) {
            sFilename = sFilename.substring(0, iLastDotPos);
         }
      }

      return sFilename;
   }
}
