package org.lcsim.event.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.lcsim.event.LCRelation;
import org.lcsim.event.RelationalTable;

/**
 * An implementation of RealtionalTable
 * @author tonyj
 */
public class BaseRelationalTable<F,T> implements RelationalTable<F, T>
{
   private enum OneMany { ONE, MANY };
   private final static double WEIGHT_ONE = 1.0;
   private final static double WEIGHT_ZERO = 0.0;
   
   private final Mode mode;
   private final Weighting weighting;
   
   private Map<Relation<F,T>,Double> weightedRelations;
   private Set<Relation<F,T>> unweightedRelations;
   
   private Map<F, Set<T>> fromMultiMap;
   private Map<T, Set<F>> toMultiMap;
   
   private Map<F,T> fromMap;
   private Map<T,F> toMap;
   
   private OneMany fromMode()
   {
      return mode == Mode.ONE_TO_MANY || mode == Mode.ONE_TO_ONE ? OneMany.ONE : OneMany.MANY;
   }
   private OneMany toMode()
   {
      return mode == Mode.MANY_TO_ONE || mode == Mode.ONE_TO_ONE ? OneMany.ONE : OneMany.MANY;
   }
   
   /** Creates an empty ManyToMany relational table, in ManyToMany mode and
    * with weights */
   public BaseRelationalTable()
   {
      this(Mode.MANY_TO_MANY,Weighting.WEIGHTED);
   }
   
   public BaseRelationalTable(Mode mode, Weighting weighting)
   {
      this.mode = mode;
      this.weighting = weighting;
      
      if (weighting == Weighting.WEIGHTED) weightedRelations = new HashMap<Relation<F,T>,Double>();
      else unweightedRelations = new HashSet<Relation<F,T>>();
      
      if (toMode() == OneMany.ONE) fromMap = new HashMap<F,T>();
      else fromMultiMap = new HashMap<F,Set<T>>();
      
      if (fromMode() == OneMany.ONE) toMap = new HashMap<T,F>();
      else toMultiMap = new HashMap<T,Set<F>>();
   }
   
   public boolean add(F from, T to)
   {
      return add(from,to,WEIGHT_ONE);
   }
   
   public boolean add(F from, T to, double weight)
   {
      if (from == null || to == null) throw new IllegalArgumentException("Argument to RelationalTable.add cannot be null");
      if (weighting == Weighting.UNWEIGHTED && weight != WEIGHT_ONE) throw new IllegalArgumentException("Weight must be 1 for unweighted relational table");
      
      boolean result = false;
      
      if (toMode() == OneMany.ONE)
      {
         T oldTo = fromMap.put(from,to);
         if (oldTo != null)
         {
            removeRelation(from,oldTo);
            result |= true;
         }
      }
      if (fromMode() == OneMany.ONE)
      {
         F oldFrom = toMap.put(to,from);
         if (oldFrom != null)
         {
            removeRelation(oldFrom,to);
            result |= true;
         }
      }
      
      // This must be done before the multimaps below to ensure ordering
      result |= addRelation(from,to,weight);
      
      if (toMode() == OneMany.MANY)
      {
         Set<T> toList = fromMultiMap.get(from);
         if (toList == null)
         {
            toList = weighting == Weighting.UNWEIGHTED ? new HashSet<T>() : new TreeSet<T>(new FromWeightComparator(from));
            fromMultiMap.put(from,toList);
         }
         toList.add(to);
      }
      
      
      if (fromMode() == OneMany.MANY)
      {
         Set<F> fromList = toMultiMap.get(to);
         if (fromList == null)
         {
            fromList = weighting == Weighting.UNWEIGHTED ? new HashSet<F>() : new TreeSet<F>(new ToWeightComparator(to));
            toMultiMap.put(to,fromList);
         }
         fromList.add(from);
      }
      
      return result;
   }
   private Set<Relation<F,T>> relations()
   {
      return weighting == Weighting.UNWEIGHTED ? unweightedRelations : weightedRelations.keySet();
   }
   private boolean removeRelation(F from, T to)
   {
      Relation relation = new Relation(from,to);
      return relations().remove(relation);
   }
   private boolean addRelation(F from, T to, double weight)
   {
      Relation relation = new Relation<F,T>(from,to);
      return weighting == Weighting.UNWEIGHTED ? !unweightedRelations.add(relation) : weightedRelations.put(relation,weight) != null;
   }
   
   public boolean remove(F from, T to)
   {
      boolean result = removeRelation(from,to);
      if (result)
      {
         if (toMode() == OneMany.ONE)
         {
            fromMap.remove(from);
         }
         else
         {
            Set<T> toList = fromMultiMap.get(from);
            toList.remove(from);
         }
         
         if (fromMode() == OneMany.ONE)
         {
            toMap.remove(to);
         }
         else
         {
            Set<T> fromList = fromMultiMap.get(to);
            fromList.remove(to);
         }
      }
      return result;
   }
   
   public T to(F from)
   {
      if (toMode() == OneMany.ONE) return fromMap.get(from);
      else
      {
         Set<T> all = allFrom(from);
         if (all.size() > 2) throw new IllegalArgumentException("Ambiguous relationship for "+from);
         return all.size() == 1 ? all.iterator().next() : null;
      }
   }
   
   public F from(T to)
   {
      if (fromMode() == OneMany.ONE) return toMap.get(to);
      else
      {
         Set<F> all = allTo(to);
         if (all.size() > 2) throw new IllegalArgumentException("Ambiguous relationship for "+to);
         return all.size() == 1 ? all.iterator().next() : null;
      }
   }
   
   public Set<T> allFrom(F from)
   {
      if (toMode() == OneMany.ONE)
      {
         T to = fromMap.get(from);
         return to == null ? Collections.<T>emptySet() : Collections.singleton(to);
      }
      else
      {
         Set<T> result = fromMultiMap.get(from);
         return result == null ? Collections.<T>emptySet() : result;
      }
   }
   
   public Map<T,Double> allFromWithWeights(F from)
   {
      if (toMode() == OneMany.ONE)
      {
         T to = fromMap.get(from);
         return to == null ? Collections.<T,Double>emptyMap() : Collections.singletonMap(to,weightFromTo(from,to));
      }
      else
      {
         Set<T> toList = fromMultiMap.get(from);
         if (toList == null || toList.isEmpty()) return Collections.<T,Double>emptyMap();
         Map<T,Double> result = new LinkedHashMap<T,Double>();
         for (T to : toList) result.put(to,weightFromTo(from,to));
         return result;
      }
   }
   
   public Set<F> allTo(T to)
   {
      if (fromMode() == OneMany.ONE)
      {
         F from = toMap.get(to);
         return from == null ? Collections.<F>emptySet() : Collections.singleton(from);
      }
      else
      {
         Set<F> result = toMultiMap.get(to);
         return result == null ? Collections.<F>emptySet() : result;
      }
   }
   
   public Map<F,Double> allToWithWeights(T to)
   {
      if (fromMode() == OneMany.ONE)
      {
         F from = toMap.get(to);
         return from == null ? Collections.<F,Double>emptyMap() : Collections.singletonMap(from,weightFromTo(from,to));
      }
      else
      {
         Set<F> fromList = toMultiMap.get(to);
         if (fromList == null || fromList.isEmpty()) return Collections.<F,Double>emptyMap();
         Map<F,Double> result = new LinkedHashMap<F,Double>();
         for (F from : fromList) result.put(from,weightFromTo(from,to));
         return result;
      }
   }
   
   public double weightFromTo(F from, T to)
   {
      Relation relation = new Relation(from,to);
      if (weighting == Weighting.UNWEIGHTED) return unweightedRelations.contains(relation) ? WEIGHT_ONE : WEIGHT_ZERO;
      else
      {
         Double d = weightedRelations.get(relation);
         return d == null ? WEIGHT_ZERO : d.doubleValue();
      }
   }
   
   public Mode getMode()
   {
      return mode;
   }
   public Weighting getWeighting()
   {
      return weighting;
   }
   public int size()
   {
      return relations().size();
   }
   
   private static class Relation<F,T>
   {
      private F from;
      private T to;
      
      Relation(F from, T to)
      {
         this.from = from;
         this.to = to;
      }
      F getFrom()
      {
         return from;
      }
      T getTo()
      {
         return to;
      }
      
      public int hashCode()
      {
         return to.hashCode() + 37*from.hashCode();
      }
      
      public boolean equals(Object obj)
      {
         if (obj instanceof Relation)
         {
            Relation that = (Relation) obj;
            return this.from.equals(that.from) && this.to.equals(that.to);
         }
         else return false;
      }
   }
   private class FromWeightComparator implements Comparator<T>
   {
      private F from;
      FromWeightComparator(F from)
      {
         this.from = from;
      }
      public int compare(T o1, T o2)
      {
         double w1 = weightFromTo(from,o1);
         double w2 = weightFromTo(from,o2);
         if (w1 == w2)
         {
            // FIXME: TreeSet bases equality on the return code from the comparator, with equal
            // elements being eliminated. Thus we only want to return 0 for object which are really equal.
            // Using hashcode is not strictly correct, since unequal object can have the same hashcode.
            return o1.hashCode() - o2.hashCode();
         }
         else return (int) Math.signum(w2 - w1);
      }
   }
   private class ToWeightComparator implements Comparator<F>
   {
      private T to;
      ToWeightComparator(T to)
      {
         this.to = to;
      }
      public int compare(F o1, F o2)
      {
         double w1 = weightFromTo(o1,to);
         double w2 = weightFromTo(o2,to);
         if (w1 == w2)
         {
            // FIXME: TreeSet bases equality on the return code from the comparator, with equal
            // elements being eliminated. Thus we only want to return 0 for object which are really equal.
            // Using hashcode is not strictly correct, since unequal object can have the same hashcode.
            return o1.hashCode() - o2.hashCode();
         }
         else return (int) Math.signum(w2 - w1);
      }
   }
   
   public void addRelations(Collection<LCRelation> relations)
   {
       for (LCRelation r : relations) {
           this.add((F)r.getFrom(), (T)r.getTo(), r.getWeight());
       }
   }
}