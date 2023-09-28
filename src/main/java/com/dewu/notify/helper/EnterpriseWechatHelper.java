package com.dewu.notify.helper;

import com.dewu.notify.model.Notify;
import org.springframework.stereotype.Component;

@Component
public class EnterpriseWechatHelper implements NotifyHelper{

	@Override
	public boolean sendNotify(Notify notify) {
		return false;
	}
}
