
package com.andrei1058.bedwars.sidebar.thread;

import com.andrei1058.bedwars.sidebar.SidebarService;

/**
 * Refresh TAB header and footer.
 */
public class RefreshTabHeaderFooterTask implements Runnable {
    @Override
    public void run() {
        SidebarService.getInstance().refreshTabHeaderFooter();
    }
}
