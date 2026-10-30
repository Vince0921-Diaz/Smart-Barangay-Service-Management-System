/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Admin;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author PC
 */
public class ReportsF extends javax.swing.JFrame {
    private final dao.ResidentDAO residentDAO=new dao.ResidentDAO(); private final dao.ServiceRequestDAO requestDAO=new dao.ServiceRequestDAO(); private final dao.AppoinmentDAO appointmentDAO=new dao.AppoinmentDAO();
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ReportsF.class.getName());

    /**
     * Creates new form ReportsF
     */
    public ReportsF(){
        initComponents();
        makeReportTablesReadOnly();
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        setLocation(0, 0);
        loadReportsAsync();
    }

    private void makeReportTablesReadOnly() {
        javax.swing.JTable[] tables = {jTable1,jTable2,jTable3,jTable4,jTable5,jTable6};
        for (javax.swing.JTable table : tables) {
            javax.swing.table.TableModel old = table.getModel();
            Object[] columns = new Object[old.getColumnCount()];
            for (int c=0;c<columns.length;c++) columns[c]=old.getColumnName(c);
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns,0) {
                @Override public boolean isCellEditable(int row,int column) { return false; }
            };
            table.setModel(model);
            table.setDefaultEditor(Object.class, null);
        }
    }

    private void loadReportsAsync(){
        javax.swing.SwingWorker<Object[],Void> worker = new javax.swing.SwingWorker<>() {
            @Override protected Object[] doInBackground() {
                java.util.List<model.Resident> residents=residentDAO.getAllResidents();
                long totalResidents=residents.size();
                long male=residents.stream().filter(x->"Male".equalsIgnoreCase(x.getGender())).count();
                long female=residents.stream().filter(x->"Female".equalsIgnoreCase(x.getGender())).count();
                long pwd=residents.stream().filter(x->"PWD".equalsIgnoreCase(x.getDisability())).count();
                long totalRequests=requestDAO.count(null);
                java.util.List<model.Appointment> appointments=appointmentDAO.getAll();
                long totalAppointments=appointments.size();

                java.util.List<Object[]> requestRows=new java.util.ArrayList<>();
                String[] requestStatuses={"Pending","Approved","Processing","Completed","Rejected","Cancelled"};
                for(String st:requestStatuses){ long n=requestDAO.count(st); double pct=totalRequests==0?0:n*100.0/totalRequests; requestRows.add(new Object[]{st,n,String.format(java.util.Locale.ROOT,"%.1f%%",pct)}); }
                requestRows.add(new Object[]{"Total Requests",totalRequests,"100.0%"});

                java.util.List<Object[]> residentRows=new java.util.ArrayList<>();
                addReportRow(residentRows,"Male",male,totalResidents);
                addReportRow(residentRows,"Female",female,totalResidents);
                residentRows.add(new Object[]{"Total Resident",totalResidents,"100.0%"});

                long completed=appointments.stream().filter(x->"Completed".equalsIgnoreCase(x.getStatus())).count();
                long pending=appointments.stream().filter(x->"Pending".equalsIgnoreCase(x.getStatus())).count();
                long cancelled=appointments.stream().filter(x->"Cancelled".equalsIgnoreCase(x.getStatus())).count();
                java.util.List<Object[]> appointmentRows=new java.util.ArrayList<>();
                addReportRow(appointmentRows,"Completed",completed,totalAppointments); addReportRow(appointmentRows,"Pending",pending,totalAppointments); addReportRow(appointmentRows,"Cancelled",cancelled,totalAppointments); appointmentRows.add(new Object[]{"Total Appointment",totalAppointments,"100.0%"});

                java.util.List<Object[]> serviceRows=new java.util.ArrayList<>();
                java.util.List<model.Service> services=new dao.ServiceDAO().getAll();
                for(model.Service svc:services){ long n=requestDAO.countByService(svc.getId()); double pct=totalRequests==0?0:n*100.0/totalRequests; serviceRows.add(new Object[]{svc.getName(),n,String.format(java.util.Locale.ROOT,"%.1f%%",pct)}); }
                serviceRows.add(new Object[]{"Total",totalRequests,"100.0%"});

                java.util.List<Object[]> monthRows=new java.util.ArrayList<>();
                String[] names={"January","February","March","April","May","June","July","August","September","October","November","December"};
                long yearTotal=0; for(int i=1;i<=12;i++){ long n=requestDAO.countByMonth(i); yearTotal+=n; monthRows.add(new Object[]{names[i-1],n}); } monthRows.add(new Object[]{"Total",yearTotal});

                java.util.List<Object[]> ageRows=new java.util.ArrayList<>();
                String[] groups={"0-17","18-30","31-45","46-60","60+"}; long[] counts=new long[5];
                for(model.Resident r:residents){ int a=r.getAge(); if(a<=17)counts[0]++; else if(a<=30)counts[1]++; else if(a<=45)counts[2]++; else if(a<60)counts[3]++; else counts[4]++; }
                for(int i=0;i<groups.length;i++)addReportRow(ageRows,groups[i],counts[i],totalResidents); ageRows.add(new Object[]{"Total",totalResidents,"100.0%"});

                return new Object[]{totalResidents,totalRequests,totalAppointments,pwd,requestRows,residentRows,appointmentRows,serviceRows,monthRows,ageRows};
            }

            @Override protected void done(){
                try {
                    Object[] d=get();
                    jLabel14.setText(String.valueOf(d[0])); jLabel15.setText(String.valueOf(d[1])); jLabel16.setText(String.valueOf(d[2])); jLabel17.setText(String.valueOf(d[3]));
                    setRows(jTable1,(java.util.List<Object[]>)d[4]); setRows(jTable2,(java.util.List<Object[]>)d[5]); setRows(jTable3,(java.util.List<Object[]>)d[6]); setRows(jTable4,(java.util.List<Object[]>)d[7]); setRows(jTable5,(java.util.List<Object[]>)d[8]); setRows(jTable6,(java.util.List<Object[]>)d[9]);
                } catch(Exception ex){ ex.printStackTrace(); javax.swing.JOptionPane.showMessageDialog(ReportsF.this,"Unable to load reports. Please check that MySQL is running.","Reports Error",javax.swing.JOptionPane.ERROR_MESSAGE); }
            }
        };
        worker.execute();
    }

    private void setRows(javax.swing.JTable table, java.util.List<Object[]> rows){
        DefaultTableModel model=(DefaultTableModel)table.getModel(); model.setRowCount(0); for(Object[] row:rows) model.addRow(row);
    }

    private static void addReportRow(java.util.List<Object[]> rows,String label,long total,long denominator){
        double pct=denominator==0?0:total*100.0/denominator; rows.add(new Object[]{label,total,String.format(java.util.Locale.ROOT,"%.1f%%",pct)});
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        btnRM = new javax.swing.JButton();
        btnSM = new javax.swing.JButton();
        btnSRM = new javax.swing.JButton();
        btnServiceRM = new javax.swing.JButton();
        btnRA = new javax.swing.JButton();
        LogOut = new javax.swing.JButton();
        btnAdmin = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setForeground(new java.awt.Color(51, 153, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(1440, 900));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1968, 390, -1, -1));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setFont(new java.awt.Font("Yu Gothic", 1, 18)); // NOI18N
        jLabel9.setText("Reports & Analytics ");

        jLabel10.setFont(new java.awt.Font("Yu Gothic", 0, 12)); // NOI18N
        jLabel10.setText("View reports and analytics for better decision making ");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9))
                .addContainerGap(943, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, 1290, 130));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Completed", null, null},
                {"Pending", null, null},
                {"Processing ", null, null},
                {"Rejected", null, null},
                {"Total Requests", null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Status ", "Total ", "Percentage"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 350, 360, 137));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Male ", null, null},
                {"Female", null, null},
                {"PWD", null, null},
                {"Total Resident", null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Category ", "Total ", "Percentage"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 370, 360, 110));

        jLabel3.setFont(new java.awt.Font("Yu Gothic Medium", 1, 14)); // NOI18N
        jLabel3.setText("Service Request Status ");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 320, -1, -1));

        jLabel4.setFont(new java.awt.Font("Yu Gothic Medium", 1, 14)); // NOI18N
        jLabel4.setText("Resiodent Overview");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 330, -1, -1));

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Completed", null, null},
                {"Pending", null, null},
                {"Cancelled", null, null},
                {"Total Appointment", "", null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Status ", "Total ", "Percentage"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        jPanel2.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1450, 410, 399, 137));

        jLabel5.setFont(new java.awt.Font("Yu Gothic Medium", 1, 14)); // NOI18N
        jLabel5.setText("Appoiontment Status");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1450, 380, -1, -1));

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Baranagay Clearance", null, null},
                {"Certificate of Indigenccy ", null, null},
                {"Business Permit", null, null},
                {"Certificate of  Residency", null, null},
                {"Certificate  of good moral ", null, null},
                {"First-Time Jobseeker Certification", null, null},
                {"Total", null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Service Type", "Total", "Percentage"
            }
        ));
        jScrollPane4.setViewportView(jTable4);

        jPanel2.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 610, 520, 167));

        jLabel6.setFont(new java.awt.Font("Yu Gothic Medium", 1, 14)); // NOI18N
        jLabel6.setText("Service  Request per month");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 520, -1, -1));

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"January", null},
                {"Feb ", null},
                {"Ma", null},
                {"Ap", null},
                {"May ", null},
                {"Ju", null},
                {"July", null},
                {"Aug", null},
                {"Sep", null},
                {"Oct", null},
                {"Nov", null},
                {"Dec", null},
                {"Total", null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Month", "Total Request"
            }
        ));
        jScrollPane5.setViewportView(jTable5);

        jPanel2.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 550, 360, 287));

        jLabel7.setFont(new java.awt.Font("Yu Gothic Medium", 1, 14)); // NOI18N
        jLabel7.setText("Service  Request by Service Type ");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 570, -1, -1));

        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"0-17", null, null},
                {"18-30", null, null},
                {"31-45", null, null},
                {"46-60", null, null},
                {"60+", null, null},
                {"Total", null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Age Group", "Total", "Percentage"
            }
        ));
        jScrollPane6.setViewportView(jTable6);

        jPanel2.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1460, 620, 360, 148));

        jLabel8.setFont(new java.awt.Font("Yu Gothic Medium", 1, 14)); // NOI18N
        jLabel8.setText("Resident Demographics");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(1457, 586, -1, -1));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel11.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        jLabel11.setText("Total Service Requests");

        jLabel15.setText("jLabel14");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                .addGap(13, 13, 13))
        );

        jPanel2.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 190, 170, -1));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel2.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        jLabel2.setText("Total Appoinmrnt");

        jLabel16.setText("jLabel14");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)))
                .addGap(29, 29, 29))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                .addGap(19, 19, 19))
        );

        jPanel2.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 190, 170, 100));

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel13.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        jLabel13.setText("PWD Resident");

        jLabel17.setText("jLabel14");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(39, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                .addGap(13, 13, 13))
        );

        jPanel2.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 190, 170, -1));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jLabel12.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        jLabel12.setText("Total Resident ");

        jLabel14.setText("jLabel14");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                .addGap(13, 13, 13))
        );

        jPanel2.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 190, -1, -1));

        jPanel12.setBackground(new java.awt.Color(0, 51, 153));
        jPanel12.setPreferredSize(new java.awt.Dimension(250, 940));

        btnRM.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        btnRM.setText("Resident Management");
        btnRM.addActionListener(this::btnRMActionPerformed);

        btnSM.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        btnSM.setText("Service Management");
        btnSM.addActionListener(this::btnSMActionPerformed);

        btnSRM.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        btnSRM.setText("Staff Management");
        btnSRM.addActionListener(this::btnSRMActionPerformed);

        btnServiceRM.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        btnServiceRM.setText("Service Request Management");
        btnServiceRM.addActionListener(this::btnServiceRMActionPerformed);

        btnRA.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        btnRA.setText("Report Analytics");
        btnRA.addActionListener(this::btnRAActionPerformed);

        LogOut.setBackground(new java.awt.Color(204, 0, 0));
        LogOut.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        LogOut.setForeground(new java.awt.Color(255, 255, 255));
        LogOut.setText("LogOut");
        LogOut.addActionListener(this::LogOutActionPerformed);

        btnAdmin.setFont(new java.awt.Font("Yu Gothic Medium", 1, 12)); // NOI18N
        btnAdmin.setText("Dashboard");
        btnAdmin.addActionListener(this::btnAdminActionPerformed);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnSRM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnServiceRM, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(btnAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnRM, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSM, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSRM, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnServiceRM, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRA, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(275, 275, 275)
                .addComponent(LogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(303, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 260, 940));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRMActionPerformed
        // TODO add your handling code here:
        ResidentManagementF rm =
        new ResidentManagementF();

        rm.setLocationRelativeTo(null);

        rm.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnRMActionPerformed

    private void btnSMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSMActionPerformed
        // TODO add your handling code here:
        ServiceManagementF SM =
        new ServiceManagementF();

        SM.setLocationRelativeTo(null);

        SM.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnSMActionPerformed

    private void btnSRMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSRMActionPerformed
        // TODO add your handling code here:
        try {
            StaffResidentManagementF SRM = new StaffResidentManagementF();
            SRM.setLocationRelativeTo(null);
            SRM.setVisible(true);
            this.dispose();
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Could not open Staff/Resident Management: " + ex.getMessage(),
                "Navigation Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnSRMActionPerformed

    private void btnServiceRMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnServiceRMActionPerformed
        try {
            ServiceRequestManagementF serviceRequestForm = new ServiceRequestManagementF();
            serviceRequestForm.setLocationRelativeTo(null);
            serviceRequestForm.setVisible(true);
            this.dispose();
        } catch (Throwable ex) {
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                "Could not open Service Request Management: " + ex.getClass().getSimpleName()
                + ": " + String.valueOf(ex.getMessage())
                + "\\nSee the NetBeans Output window for details.",
                "Navigation Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnServiceRMActionPerformed

    private void btnRAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRAActionPerformed
        // TODO add your handling code here:
        ReportsF RA =
        new ReportsF();

        RA.setLocationRelativeTo(null);

        RA.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnRAActionPerformed

    private void LogOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogOutActionPerformed
        // TODO add your handling code here:
        int confirm =
        javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit",
            javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (confirm ==
            javax.swing.JOptionPane.YES_OPTION) {

            System.exit(0);
        }
    }//GEN-LAST:event_LogOutActionPerformed

    private void btnAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdminActionPerformed
        AdminDashboard nextScreen = new AdminDashboard(); nextScreen.setLocationRelativeTo(null); nextScreen.setVisible(true); this.dispose();
    }//GEN-LAST:event_btnAdminActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ReportsF().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton LogOut;
    private javax.swing.JButton btnAdmin;
    private javax.swing.JButton btnRA;
    private javax.swing.JButton btnRM;
    private javax.swing.JButton btnSM;
    private javax.swing.JButton btnSRM;
    private javax.swing.JButton btnServiceRM;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTable6;
    // End of variables declaration//GEN-END:variables
}
