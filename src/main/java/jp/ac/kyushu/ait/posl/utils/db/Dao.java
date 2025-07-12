package jp.ac.kyushu.ait.posl.utils.db;

import jp.ac.kyushu.ait.posl.utils.log.MyLogger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class store any types of class, using hibernate.
 * @param <T>
 */
public class Dao<T> {
	EntityManager entityManager;
	EntityManagerFactory entityManagerFactory;
	/**
	 * this name corresponds to the attribute in persistence.xml
	 */
	public static String profileName = "repos";

	/**
	 * SQL condition
	 */
	List<String> where;
	/**
	 * The number of instances to get
	 */
	Integer limit;
	/**
	 * this variable is used to lock the row in the table
	 */
	LockModeType lock;
	String orderBy = "";
	String groupBy = "";

	protected static MyLogger log = MyLogger.getInstance();

	protected Class<T> parameterType;
	public Dao(T... dummy) {
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) dummy.getClass().getComponentType();
		parameterType = type;
	}


	public Class<T> getParameterType() {
		return parameterType;
	}


	/**
	 * This method must be used before any commands
	 * @param repoName
	 */
	public void init(String repoName) {
		this.where = new ArrayList<>();
		try {
			this.entityManagerFactory = Persistence.createEntityManagerFactory(repoName);
			this.entityManager = this.entityManagerFactory.createEntityManager();
		} catch (Error e) {
			System.err.println(e.getCause().getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				System.err.println(s);
			}
			throw new RuntimeException();
		}
	}

	public void showDatabaseSetting() {
		if(this.entityManagerFactory==null){
			System.err.println("WARN: entityManagerFactory in Dao needs to be initialize first before showDatabaseSetting");
			return;
		}
		Map<String, Object> properties = this.entityManagerFactory.getProperties();

		try{
			System.out.println("****************************");
			System.out.println("URL: "+ properties.get("javax.persistence.jdbc.url"));
			System.out.println("Batch_size: "+ properties.get("hibernate.jdbc.batch_size"));
			System.out.println("Hbm2ddl: "+ properties.get("hibernate.hbm2ddl.auto"));
			System.out.println("Pool_size: "+ properties.get("hibernate.connection.pool_size"));
			System.out.println("Driver: "+ properties.get("hibernate.connection.driver_class"));
			System.out.println("****************************");
		}catch (Throwable t){
			//To avoid crash
		}
	}

	public void init() {
		init(profileName);
	}

	public void setLimit(Integer i){
		this.limit = i;
	}


	/**
	 * get all instances
	 */
	public List<T> select() {
		List<T> result = null;
		System.out.println("from "+parameterType.getName() );
		System.out.println(this.createWhere());
		Query query  = this.entityManager.createQuery("from "+parameterType.getName() + " " + this.createWhere()+groupBy+orderBy, parameterType);
		if(limit!=null){
			query.setMaxResults(limit);
		}
		if(lock!=null) {
			query.setLockMode(this.lock);
		}
		result = query.getResultList();
		return result;
	}

	/**
	 * to select with different condition
	 */
	public void flushWhere(){
		this.where = new ArrayList<>();
		orderBy = "";
		groupBy = "";
	}

	/**
	 * this creates a condition for string
	 * @param key
	 * @param value
	 */
	public void setWhere(String key, String value){
		if(value == null){
			System.err.println("null is found in setWhere");
			throw new RuntimeException();
		}else{
			where.add(" "+ key +" = '"+value+"' ");
		}
	}
	/**
	 * this creates a condition for boolean
	 * @param key
	 * @param value
	 */
	public void setWhere(String key, Boolean value){
		if(value == null){
			System.err.println("null is found in setWhere");
			throw new RuntimeException();
		}else{
			where.add(" "+ key +" = "+value );
		}
	}
	public void setWhere(String key, Long value){
		if(value == null){
			System.err.println("null is found in setWhere");
			throw new RuntimeException();
		}else{
			where.add(" "+ key +" = "+value );
		}
	}
	/**
	 * this creates a condition for Integer
	 * @param key
	 * @param value
	 */
	public void setWhere(String key, Integer value){
		if(value == null){
			System.err.println("null is found in setWhere");
			throw new RuntimeException();
		}else{
			where.add(" "+ key +" = "+value );
		}
	}




	protected void setWhere(String key, int i) {
		where.add(" "+ key +" = "+ i +" ");
	}

	public void setWhereIn(String key, List<String> values){
		if(values == null){
			System.err.println("null is found in setWhere");
		}else if(values.size()>0){
			StringBuilder clause = new StringBuilder();
			for (String v: values){
				if(!clause.toString().equals("")){
					clause.append(",");
				}
				clause.append("'").append(v).append("'");
			}
			where.add(" "+ key +" IN ("+clause+") ");

		}
	}

	/**
	 * This merges all the conditions before applying SQL
	 * @return
	 */
	private String createWhere() {
		String clause = "";
		int i = 0;
		for(String w: where){
			if(i==0){
				clause += " where ";
			}else{
				clause += " AND ";
			}
			clause += w;
			i++;
		}
		return clause;
	}

	/**
	 * Remove the data from database
	 * 
	 * @param t
	 */
	public void remove(T t) {
		try {
			this.entityManager.getTransaction().begin();
			this.entityManager.remove(t);
			this.entityManager.getTransaction().commit();

		} catch (Exception e) {
			System.err.println(e);
			System.err.println(e.getStackTrace());
			if (this.entityManager.getTransaction().isActive())
				this.entityManager.getTransaction().rollback();
			this.close();
			throw e;
		}
	}

	/**
	 * update the database
	 * 
	 * @param t
	 */
	public void update(T t) {
		try {
			entityManager.getTransaction().begin();
			entityManager.merge(t);
			entityManager.getTransaction().commit();
		} catch (Exception e) {
			System.err.println(e);
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			this.close();
		}
	}
	/**
	 * update the database
	 *
	 * @param list
	 */
	public void update(List<T> list) {
		for(T t: list){
			this.update(t);
		}
	}

	/**
	 * store the data
	 * 
	 * @param t
	 */
	public void insert(T t) {
		try {
			this.entityManager.getTransaction().begin();
			this.entityManager.persist(t);
			this.entityManager.getTransaction().commit();
		} catch (Throwable e) {
			log.error(e);
			System.err.println(e.getLocalizedMessage());
			for (StackTraceElement i : e.getStackTrace()) {
				System.err.println(i.toString());
			}
			if (this.entityManager != null && this.entityManager.getTransaction().isActive())
				this.entityManager.getTransaction().rollback();
			throw e;
		}
	}

	/**
	 * close all the database resources
	 */
	public void close() {
		if (entityManager != null) {
			entityManager.close();
		}
		if (entityManagerFactory != null) {
			entityManagerFactory.close();
		}


	}

	/**
	 * store the list data
	 * @param list
	 */
	public void insert(List<T> list) {
		try {
			this.entityManager.getTransaction().begin();
			int i = 1;
			for (T t : list) {
				// System.out.println(t.toString());
				try {
					this.entityManager.persist(t);

					if (i % 100 == 0) { // should be the same number as batch size
						this.entityManager.flush();
						this.entityManager.clear();
						i = 0;
					}
					i++;
				} catch (Exception e1) {
					System.err.println(e1);
					throw new Error(e1);
				}
			}
			this.entityManager.getTransaction().commit();
		} catch (Exception e) {
			log.error(e);
			for (StackTraceElement i : e.getStackTrace()) {
				System.err.println(i.toString());
			}
			if (this.entityManager != null && this.entityManager.getTransaction().isActive())
				this.entityManager.getTransaction().rollback();
			this.close();
			throw new Error(e);
		}
	}
	public void truncate(String table) {
		this.entityManager.getTransaction().begin();
		this.entityManager.createNativeQuery("truncate table "+table).executeUpdate();
		this.entityManager.getTransaction().commit();
	}
	public void truncateCascade(String table) {
		this.entityManager.getTransaction().begin();
		this.entityManager.createNativeQuery("truncate table "+table+ " CASCADE").executeUpdate();
		this.entityManager.getTransaction().commit();
	}


	protected void setLock(LockModeType lock) {
		this.lock = lock;
	}

    public void remove(String table, String key, String value) {
		this.entityManager.getTransaction().begin();
		String hqlDelete = "delete "+table+" where "+key+" = :val";
		int deletedEntities = entityManager.createQuery( hqlDelete )
				.setParameter("val", value)
				.executeUpdate();
		this.entityManager.getTransaction().commit();
    }

	public void enableRandomFetch(){
		this.orderBy = " ORDER BY random() ";
	}
    public void setOrderBy(String key){
		this.orderBy = " order by "+key +" DESC ";
	}
	public void setGroupBy(String key){
		this.groupBy = " group by "+key;
	}

}
