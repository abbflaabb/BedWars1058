
package com.andrei1058.bedwars.sidebar.thread;

import com.andrei1058.bedwars.sidebar.SidebarService;

public class RefreshPlaceholdersTask implements Runnable {
    @Override
    public void run() {
        SidebarService.getInstance().refreshPlaceholders();
    }
}
