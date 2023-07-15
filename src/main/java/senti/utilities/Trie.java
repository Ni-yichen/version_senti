package senti.utilities;

public class Trie {//数据结构的工具类。字典树是一种树形数据结构，用于高效存储和检索字符串集合

   /**
    *方法用于查找某个字符串在字典树中的位置，如果字符串不在树中则可以选择将其插入到树中。
    * @param sText 查找/插入的字符串
    * @param sArray 用于存储字符串的数组
    * @param iLessPointer 存储trie树节点的左子节点索引的数组，如果一个元素没有左子节点，则其指针值为-1。
    * @param iMorePointer 存储trie树节点的右子节点索引的数组，如果一个元素没有右子节点，则其指针值为-1。
    * @param iFirstElement 要查找的元素的数组中的起始位置。
    * @param iLastElement 要查找的元素的数组中的结束位置。
    * @param bDontAddNewString 示是否应将字符串插入到数组中，如果为false，则返回-1而不插入。
    * @return 目标字符串在字典树中的位置，如果字符串不在树中且不允许添加新字符串则返回-1
    */
   public static int i_GetTriePositionForString(String sText, String[] sArray, int[] iLessPointer, int[] iMorePointer, int iFirstElement, int iLastElement, boolean bDontAddNewString) {
      int iTriePosition = 0;
      int iLastTriePosition = 0;
      if (iLastElement < iFirstElement) {
         sArray[iFirstElement] = sText;
         iLessPointer[iFirstElement] = -1;
         iMorePointer[iFirstElement] = -1;
         return iFirstElement;
      } else {
         iTriePosition = iFirstElement;

//         int iLastTriePosition;
         label33:
         do {
            do {
               iLastTriePosition = iTriePosition;
               if (sText.compareTo(sArray[iTriePosition]) < 0) {
                  iTriePosition = iLessPointer[iTriePosition];
                  continue label33;
               }

               if (sText.compareTo(sArray[iTriePosition]) <= 0) {
                  return iTriePosition;
               }

               iTriePosition = iMorePointer[iTriePosition];
            } while(iTriePosition != -1);

            if (bDontAddNewString) {
               return -1;
            }

            ++iLastElement;
            sArray[iLastElement] = sText;
            iLessPointer[iLastElement] = -1;
            iMorePointer[iLastElement] = -1;
            iMorePointer[iLastTriePosition] = iLastElement;
            return iLastElement;
         } while(iTriePosition != -1);

         if (bDontAddNewString) {
            return -1;
         } else {
            ++iLastElement;
            sArray[iLastElement] = sText;
            iLessPointer[iLastElement] = -1;
            iMorePointer[iLastElement] = -1;
            iLessPointer[iLastTriePosition] = iLastElement;
            return iLastElement;
         }
      }
   }

   /**
    * 方法与上面的方法类似，但是使用了不同的参数和实现方式，用于旧版代码的兼容。
    * @param sText 查找/插入的字符串
    * @param sArray 用于存储字符串的数组
    * @param iLessPointer 存储trie树节点的左子节点索引的数组，如果一个元素没有左子节点，则其指针值为-1。
    * @param iMorePointer 存储trie树节点的右子节点索引的数组，如果一个元素没有右子节点，则其指针值为-1。
    * @param iLastElement 要查找的元素的数组中的结束位置。
    * @param bDontAddNewString 示是否应将字符串插入到数组中，如果为false，则返回-1而不插入。
    * @return 目标字符串在字典树中的位置，如果字符串不在树中且不允许添加新字符串则返回-1
    */
   public static int i_GetTriePositionForString_old(String sText, String[] sArray, int[] iLessPointer, int[] iMorePointer, int iLastElement, boolean bDontAddNewString) {
      int iTriePosition = 0;
      int iLastTriePosition = 0;
      if (iLastElement == 0) {
         iLastElement = 1;
         sArray[iLastElement] = sText;
         iLessPointer[iLastElement] = 0;
         iMorePointer[iLastElement] = 0;
         return 1;
      } else {
         iTriePosition = 1;

//         int iLastTriePosition;
         label33:
         do {
            do {
               iLastTriePosition = iTriePosition;
               if (sText.compareTo(sArray[iTriePosition]) < 0) {
                  iTriePosition = iLessPointer[iTriePosition];
                  continue label33;
               }

               if (sText.compareTo(sArray[iTriePosition]) <= 0) {
                  return iTriePosition;
               }

               iTriePosition = iMorePointer[iTriePosition];
            } while(iTriePosition != 0);

            if (bDontAddNewString) {
               return 0;
            }

            ++iLastElement;
            sArray[iLastElement] = sText;
            iLessPointer[iLastElement] = 0;
            iMorePointer[iLastElement] = 0;
            iMorePointer[iLastTriePosition] = iLastElement;
            return iLastElement;
         } while(iTriePosition != 0);

         if (bDontAddNewString) {
            return 0;
         } else {
            ++iLastElement;
            sArray[iLastElement] = sText;
            iLessPointer[iLastElement] = 0;
            iMorePointer[iLastElement] = 0;
            iLessPointer[iLastTriePosition] = iLastElement;
            return iLastElement;
         }
      }
   }

   /**
    * 方法与第一个方法类似，但是增加了一个计数功能，用于记录每个字符串在集合中出现的次数。
    * 方法会在字符串的位置上增加计数值。
    * @param sText 需要在trie树中查找的字符串
    * @param sArray 存储在trie树中的字符串数组
    * @param iCountArray 存储字典树节点对应的字符串出现次数的数组
    * @param iLessPointer 存储trie树节点的左子节点索引的数组
    * @param iMorePointer 存储trie树节点的右子节点索引的数组
    * @param iFirstElement 字典树数组中第一个元素的下标。
    * @param iLastElement 字典树数组中最后一个元素的下标。
    * @param bDontAddNewString 是否允许向字典树中添加新的字符串
    * @param iCount 要将字符串的计数器初始化为的值
    * @return 目标字符串在字典树中的位置，如果字符串不在树中且不允许添加新字符串则返回-1
    */
   public static int i_GetTriePositionForStringAndAddCount(String sText, String[] sArray, int[] iCountArray, int[] iLessPointer, int[] iMorePointer, int iFirstElement, int iLastElement, boolean bDontAddNewString, int iCount) {
      int iPos = i_GetTriePositionForString(sText, sArray, iLessPointer, iMorePointer, iFirstElement, iLastElement, bDontAddNewString);
      if (iPos >= 0) {
         int var10002 = iCountArray[iPos]++;
      }
      return iPos;
   }
}
