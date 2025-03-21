package com.dashboard.financeiro.service.report;

import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse;
import com.dashboard.financeiro.dto.summary.FinancialSummaryResponse.CategorySummaryDto;
import com.dashboard.financeiro.model.Transaction;
import com.dashboard.financeiro.model.User;
import com.dashboard.financeiro.repository.TransactionRepository;
import com.dashboard.financeiro.repository.UserRepository;
import com.dashboard.financeiro.service.FinancialSummaryService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
public class FinancialReportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialSummaryService financialSummaryService;

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);

    public ByteArrayOutputStream generateFinancialReport(String username, LocalDate startDate, LocalDate endDate) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }

        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }

        // Obter transações do usuário
        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        // Obter resumo financeiro
        FinancialSummaryResponse summary = financialSummaryService.getFinancialSummary(username, startDate, endDate);

        // Criar o documento PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();
        addMetadata(document);
        addHeader(document, user, startDate, endDate);
        addFinancialSummary(document, summary);
        addTransactionsTable(document, transactions);
        addCategoryCharts(document, summary);
        document.close();

        return baos;
    }

    private void addMetadata(Document document) {
        document.addTitle("Relatório Financeiro");
        document.addSubject("Relatório de Finanças Pessoais");
        document.addKeywords("finanças, despesas, receitas, relatório");
        document.addAuthor("Dashboard Financeiro");
        document.addCreator("Dashboard Financeiro - Sistema de Gestão Financeira");
    }

    private void addHeader(Document document, User user, LocalDate startDate, LocalDate endDate) throws DocumentException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        Paragraph title = new Paragraph("Relatório Financeiro", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Paragraph userInfo = new Paragraph("Usuário: " + user.getUsername(), NORMAL_FONT);
        document.add(userInfo);
        
        Paragraph period = new Paragraph(
                "Período: " + startDate.format(formatter) + " até " + endDate.format(formatter), 
                NORMAL_FONT);
        period.setSpacingAfter(20);
        document.add(period);
    }

    private void addFinancialSummary(Document document, FinancialSummaryResponse summary) throws DocumentException {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        Paragraph summaryTitle = new Paragraph("Resumo Financeiro", SUBTITLE_FONT);
        summaryTitle.setSpacingBefore(15);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Estilo para células
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new BaseColor(51, 102, 153));
        headerCell.setPadding(5);

        // Adicionar títulos
        headerCell.setPhrase(new Phrase("Item", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Valor", HEADER_FONT));
        table.addCell(headerCell);

        // Adicionar dados do resumo
        addSummaryRow(table, "Saldo Atual", currencyFormatter.format(summary.getCurrentBalance()));
        addSummaryRow(table, "Total de Receitas", currencyFormatter.format(summary.getTotalIncome()));
        addSummaryRow(table, "Total de Despesas", currencyFormatter.format(summary.getTotalExpense()));

        document.add(table);
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        table.addCell(new Phrase(label, NORMAL_FONT));
        table.addCell(new Phrase(value, NORMAL_FONT));
    }

    private void addTransactionsTable(Document document, List<Transaction> transactions) throws DocumentException {
        Paragraph transactionsTitle = new Paragraph("Transações do Período", SUBTITLE_FONT);
        transactionsTitle.setSpacingBefore(20);
        transactionsTitle.setSpacingAfter(10);
        document.add(transactionsTitle);

        if (transactions.isEmpty()) {
            document.add(new Paragraph("Nenhuma transação encontrada no período selecionado.", NORMAL_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        
        // Definir larguras relativas das colunas
        float[] columnWidths = {0.7f, 2f, 1.5f, 1.2f, 1.2f};
        table.setWidths(columnWidths);

        // Estilo para células de cabeçalho
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new BaseColor(51, 102, 153));
        headerCell.setPadding(5);

        // Adicionar cabeçalhos
        headerCell.setPhrase(new Phrase("Data", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Descrição", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Categoria", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Tipo", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Valor", HEADER_FONT));
        table.addCell(headerCell);

        // Formatadores
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Adicionar linhas de transações
        for (Transaction transaction : transactions) {
            table.addCell(new Phrase(transaction.getDate().format(dateFormatter), NORMAL_FONT));
            table.addCell(new Phrase(transaction.getDescription(), NORMAL_FONT));
            
            String categoryName = transaction.getCategory() != null ? 
                    transaction.getCategory().getName() : "Sem categoria";
            table.addCell(new Phrase(categoryName, NORMAL_FONT));
            
            table.addCell(new Phrase(transaction.getType().getDescription(), NORMAL_FONT));
            
            PdfPCell valueCell = new PdfPCell(new Phrase(
                    currencyFormatter.format(transaction.getAmount()), NORMAL_FONT));
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(valueCell);
        }

        document.add(table);
    }

    private void addCategoryCharts(Document document, FinancialSummaryResponse summary) throws DocumentException, IOException {
        if (summary.getExpensesByCategory() == null || summary.getExpensesByCategory().isEmpty()) {
            return;
        }

        Paragraph chartsTitle = new Paragraph("Gráficos", SUBTITLE_FONT);
        chartsTitle.setSpacingBefore(20);
        chartsTitle.setSpacingAfter(10);
        document.add(chartsTitle);

        // Criar gráfico de pizza para despesas por categoria
        JFreeChart chart = createPieChart("Despesas por Categoria", summary.getExpensesByCategory());
        
        // Converter o gráfico para imagem
        ByteArrayOutputStream chartImageStream = new ByteArrayOutputStream();
        BufferedImage chartImage = chart.createBufferedImage(500, 300);
        ImageIO.write(chartImage, "png", chartImageStream);
        
        // Adicionar a imagem ao documento
        Image image = Image.getInstance(chartImageStream.toByteArray());
        image.setAlignment(Element.ALIGN_CENTER);
        image.scalePercent(75);
        document.add(image);

        // Adicionar tabela com detalhes de despesas por categoria
        addCategoryTable(document, "Despesas por Categoria", summary.getExpensesByCategory());

        // Se houver categorias de receitas, adicionar também
        if (summary.getIncomesByCategory() != null && !summary.getIncomesByCategory().isEmpty()) {
            // Criar gráfico de pizza para receitas por categoria
            JFreeChart incomeChart = createPieChart("Receitas por Categoria", summary.getIncomesByCategory());
            
            // Converter o gráfico para imagem
            ByteArrayOutputStream incomeChartImageStream = new ByteArrayOutputStream();
            BufferedImage incomeChartImage = incomeChart.createBufferedImage(500, 300);
            ImageIO.write(incomeChartImage, "png", incomeChartImageStream);
            
            // Adicionar a imagem ao documento
            Image incomeImage = Image.getInstance(incomeChartImageStream.toByteArray());
            incomeImage.setAlignment(Element.ALIGN_CENTER);
            incomeImage.scalePercent(75);
            document.add(incomeImage);
            
            // Adicionar tabela com detalhes de receitas por categoria
            addCategoryTable(document, "Receitas por Categoria", summary.getIncomesByCategory());
        }
    }

    private JFreeChart createPieChart(String title, List<CategorySummaryDto> categories) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (CategorySummaryDto category : categories) {
            dataset.setValue(category.getCategoryName(), category.getAmount().doubleValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
                title,       // Título
                dataset,     // Dataset
                true,        // Legenda
                true,        // Tooltips
                false        // URLs
        );
        
        // Personalizar o gráfico
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setShadowPaint(null);
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        
        return chart;
    }

    private void addCategoryTable(Document document, String title, List<CategorySummaryDto> categories) throws DocumentException {
        Paragraph tableTitle = new Paragraph(title, NORMAL_FONT);
        tableTitle.setSpacingBefore(10);
        tableTitle.setSpacingAfter(5);
        document.add(tableTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Estilo para células de cabeçalho
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new BaseColor(51, 102, 153));
        headerCell.setPadding(5);

        // Adicionar cabeçalhos
        headerCell.setPhrase(new Phrase("Categoria", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Valor", HEADER_FONT));
        table.addCell(headerCell);

        // Formatador de moeda
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Adicionar linhas de categorias
        for (CategorySummaryDto category : categories) {
            table.addCell(new Phrase(category.getCategoryName(), NORMAL_FONT));
            
            PdfPCell valueCell = new PdfPCell(new Phrase(
                    currencyFormatter.format(category.getAmount()), NORMAL_FONT));
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(valueCell);
        }

        document.add(table);
    }
}
