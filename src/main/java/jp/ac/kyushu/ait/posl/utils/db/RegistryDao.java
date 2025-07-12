package jp.ac.kyushu.ait.posl.utils.db;

import jp.ac.kyushu.ait.posl.beans.run.Registry;

import javax.persistence.LockModeType;
import java.util.List;

public class RegistryDao <T extends Registry> extends Dao<T> {
	
	public RegistryDao(T... dummy) {
		Class<T> type = (Class<T>) dummy.getClass().getComponentType();
		parameterType = type;
	}

	/**
	 * this gets a registry with a lock
	 * to prevent from building the same instances on Kubernetes at the same time
	 * @return
	 */
	public T getOneRegistry() {
		return this.reRunOne(0);
    }

    public T reRunOne(int status){
		T result = null;
		try {
			this.entityManager.getTransaction().begin();
			this.setWhere("result_code", status);
			this.enableRandomFetch();
			this.setLimit(1);
			this.setLock(LockModeType.PESSIMISTIC_READ);//lock
			List<T> selected = this.select();
			if(selected != null){
				result = selected.get(0);
				result.startNow();//This is a temporal date
				result.resultCode = 1;
				result.errorMessage=null;
				result.resultMessage=null;
				entityManager.merge(result);//change status to running
				this.entityManager.getTransaction().commit();
			}else{
				this.entityManager.getTransaction().rollback();
			}
		}catch (final Exception e) {
			if (this.entityManager.getTransaction().isActive())
				this.entityManager.getTransaction().rollback();
			//need not throw Exception
		}
		return result;
	}

	
	public T getProjectFromRegistry() {
		T result = null;
		try {
			this.entityManager.getTransaction().begin();
			this.enableRandomFetch();
			this.setLimit(1);
			this.setLock(LockModeType.PESSIMISTIC_READ);//lock
			List<T> selected = this.select();
			if(selected != null){
				result = selected.get(0);
				result.resultCode = 1;
				entityManager.merge(result);//change status to running
				this.entityManager.getTransaction().commit();
			}else{
				this.entityManager.getTransaction().rollback();
			}
			this.entityManager.getTransaction().commit();
		}catch (final Exception e) {
			if (this.entityManager.getTransaction().isActive())
				this.entityManager.getTransaction().rollback();
			//need not throw Exception
		}
		return result;
    }
}