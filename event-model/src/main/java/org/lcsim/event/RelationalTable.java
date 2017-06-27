package org.lcsim.event;

import java.util.Map;
import java.util.Set;

/**
 * A relational table allows associations between objects. It is designed to 
 * be persisted to/from LCIO files as a collection of LCRelations, but to add
 * considerably more functionality than a raw collection of relations.
 * 
 * It can be set to mode 1-1, 1-n, n-1, n-m. When adding new relations they will
 * silently replace existing relationships based on the mode.
 *
 * It allows weights to be associated with each relation, unless it is set to 
 * weighting mode UNWEIGHTED.
 *
 * @author tonyj
 */
public interface RelationalTable<F, T>
{
   public enum Weighting { WEIGHTED, UNWEIGHTED };
   public enum Mode 
   {
      ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY;
   }
   /**
    * Add a new unweighted relation.
    * @returns <code>true</code> if this relationship replaces an existing relationship, <code>false</code> otherwise.
    */
   boolean add(F from, T to);

   /**
    * Add a new weighted relation.
    * @returns <code>true</code> if this relationship replaces an existing relationship, <code>false</code> otherwise.
    * @throws IllegalArgumentExeption if table in unweighted and weight is not 1
    */
   boolean add(F from, T to, double weight);

   /**
    * Returns the list of <code>to</code> objects corresponding to a given <code>from</code> object.
    * The natural ordering of the elements will be in decreasing weight.
    */
   Set<T> allFrom(F from);

   /**
    * Returns the map of <code>to</code> objects in the table, with their weights.
    * The natural ordering of the keys will be by decreasing weight.
    */
   Map<T, Double> allFromWithWeights(F from);

   /**
    * Returns the list of <code>from</code> objects corresponding to a given <code>to</code> object.
    * If the table is weighted, the returned list will be returned with the highest
    * weighting first.
    */
   Set<F> allTo(T to);
   /**
    * Returns the map of <code>from</code> objects in the table, with their weights.
    * The natural ordering of the keys will be by decreasing weight.
    */
   Map<F, Double> allToWithWeights(T to);

   /**
    * Gets the (unique) <code>from</code> object corresponding to object <code>to</code>.
    * @throws IllegalArgumentException if more than one to object corresponds to <code>to</code>
    */
   F from(T to);

   Mode getMode();

   Weighting getWeighting();

   /**
    * Remove any relationship between <code>from</code> and <code>to</code>
    * @return <code>true</code> if the table contained the relationship
    */
   boolean remove(F from, T to);

   /**
    * Returns the total number of relationships in this table.
    */
   int size();

   /**
    * Gets the (unique) <code>to</code> object corresponding to object <code>from</code>.
    * @throws IllegalArgumentException if more than one to object corresponds to <code>from</code>
    */
   T to(F from);

   /**
    * Returns the weight of the relationship between <code>from</code> and <code>to</code>
    * Returns 0.0 if there is no relationship, and 1.0 if there is an unweighted relationship.
    */
   double weightFromTo(F from, T to);
   
}
