package org.ovirt.engine.ui.frontend;

import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

public class AsyncQuery {
	public Object Model;
	public  INewAsyncCallback asyncCallback;
	public IAsyncConverter converterCallback;
	private boolean handleFailure;	
	public VdcQueryReturnValue OriginalReturnValue;
	public Object[] Data;
	private String context;
	
	public AsyncQuery() {
	}
	
	public AsyncQuery(Object target, INewAsyncCallback asyncCallback) {
	    setModel(target);
	    this.asyncCallback = asyncCallback;
    }
	
	public AsyncQuery(Object target, INewAsyncCallback asyncCallback, boolean handleFailure) {
        setModel(target);
        this.asyncCallback = asyncCallback;
        this.handleFailure = handleFailure;
    }
	
	public AsyncQuery(Object target, INewAsyncCallback asyncCallback, String context) {
        setModel(target);
        this.asyncCallback = asyncCallback;
        this.context = context;
    }
	
	public Object[] getData() {
		return Data;
	}
	public void setData(Object[] data) {
		this.Data = data;
	}
	public VdcQueryReturnValue getOriginalReturnValue() {
		return OriginalReturnValue;
	}
	public void setOriginalReturnValue(VdcQueryReturnValue originalReturnValue) {
		this.OriginalReturnValue = originalReturnValue;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	
	public boolean isHandleFailure() {
        return handleFailure;
    }
    public void setHandleFailure(boolean handleFailure) {
        this.handleFailure = handleFailure;
    }
    
	public Object getModel() {
		return Model;
	}
	public void setModel(Object model) {
		this.Model = model;
	}
	public INewAsyncCallback getDel() {
		return asyncCallback;
	}
	public void setDel(INewAsyncCallback asyncCallback) {
		this.asyncCallback = asyncCallback;
	}
	
	public IAsyncConverter getConverter(){
		return converterCallback;	
	}
}
