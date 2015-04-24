package com.linkedin.pinot.core.operator.filter.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;
import org.roaringbitmap.buffer.MutableRoaringBitmap;

import com.linkedin.pinot.core.segment.index.InvertedIndexReader;


public class BitmapUtils {
  private static final Logger logger = Logger.getLogger(BitmapUtils.class);

  public static MutableRoaringBitmap fastAnd(final ImmutableRoaringBitmap[] bitmaps) {
    long start = System.currentTimeMillis();
    MutableRoaringBitmap answer;

    if (bitmaps.length == 1) {
      answer = new MutableRoaringBitmap();
      answer.and(bitmaps[0]);
    } else if (bitmaps.length == 2) {
      answer = ImmutableRoaringBitmap.and(bitmaps[0], bitmaps[1]);
    } else {
      //if we have more than 2 bitmaps to intersect, re order them so that we use the bitmaps according to the number of bits set to 1
      final Integer array[] = new Integer[bitmaps.length];
      for (int i = 0; i < array.length; i++) {
        array[i] = i;
      }
      Arrays.sort(array, new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
          return bitmaps[o1].getSizeInBytes() - bitmaps[o2].getSizeInBytes();
        }
      });
      answer = ImmutableRoaringBitmap.and(bitmaps[0], bitmaps[1]);
      for (int srcId = 2; srcId < bitmaps.length; srcId++) {
        answer.and(bitmaps[srcId]);
      }
    }
    boolean validate = false;
    if (validate) {
      final MutableRoaringBitmap bit = (MutableRoaringBitmap) bitmaps[0];
      for (int srcId = 1; srcId < bitmaps.length; srcId++) {
        final MutableRoaringBitmap bitToAndWith = (MutableRoaringBitmap) bitmaps[srcId];
        bit.and(bitToAndWith);
      }
      if (!answer.equals(bit)) {
        logger.error("Optimized result differs from unoptimized solution, \n\t optimized: " + answer
            + " \n\t unoptimized: " + bit);
      }
    }
    long end = System.currentTimeMillis();
    logger.debug("And operator took: " + (end - start));

    return answer;
  }

  public static MutableRoaringBitmap fastOr(final ImmutableRoaringBitmap[] bitmaps) {
    long start = System.currentTimeMillis();
    MutableRoaringBitmap answer;
    if (bitmaps.length == 1) {
      answer = new MutableRoaringBitmap();
      answer.or(bitmaps[0]);
    } else if (bitmaps.length == 2) {
      answer = ImmutableRoaringBitmap.or(bitmaps[0], bitmaps[1]);
    } else {
      //if we have more than 2 bitmaps to intersect, re order them so that we use the bitmaps according to the number of bits set to 1
      PriorityQueue<ImmutableRoaringBitmap> pq =
          new PriorityQueue<ImmutableRoaringBitmap>(bitmaps.length, new Comparator<ImmutableRoaringBitmap>() {
            @Override
            public int compare(ImmutableRoaringBitmap a, ImmutableRoaringBitmap b) {
              return a.getSizeInBytes() - b.getSizeInBytes();
            }
          });
      for (int srcId = 0; srcId < bitmaps.length; srcId++) {
        pq.add(bitmaps[srcId]);
      }
      ImmutableRoaringBitmap x1 = pq.poll();
      ImmutableRoaringBitmap x2 = pq.poll();
      answer = ImmutableRoaringBitmap.or(x1, x2);
      while (pq.size() > 0) {
        answer.or(pq.poll());
      }
    }
    //turn this on manually if we want to compare optimized and unoptimized version
    boolean validate = false;
    if (validate) {
      final MutableRoaringBitmap bit = (MutableRoaringBitmap) bitmaps[0];
      for (int srcId = 1; srcId < bitmaps.length; srcId++) {
        final MutableRoaringBitmap bitToAndWith = (MutableRoaringBitmap) bitmaps[srcId];
        bit.or(bitToAndWith);
      }

    }
    long end = System.currentTimeMillis();

    logger.debug("time taken for fast Or : " + (end - start));
    return answer;
  }

  public static ImmutableRoaringBitmap getOrBitmap(InvertedIndexReader invertedIndex, List<Integer> idsToOr) {
    ImmutableRoaringBitmap bm;
    if (idsToOr.size() == 0) {
      bm = new MutableRoaringBitmap();
    }

    ImmutableRoaringBitmap[] bitmaps = new ImmutableRoaringBitmap[idsToOr.size()];
    for (int i = 0; i < idsToOr.size(); i++) {
      bitmaps[i] = invertedIndex.getImmutable(idsToOr.get(i));
    }

    bm = BitmapUtils.fastOr(bitmaps);
    return bm;
  }
}
