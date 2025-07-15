
package com.andrei1058.bedwars.sidebar.thread;

import com.andrei1058.bedwars.sidebar.SidebarService;

public class RefreshLifeTask implements Runnable {
    @Override
    public void run() {
        SidebarService.getInstance().refreshHealth();
    }
}
