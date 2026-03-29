# E-Commerce Frontend - AI Coding Instructions

## Architecture Overview

This is a **React-based e-commerce frontend** using Create React App with the following key architectural patterns:

### Core Structure
- **Context-based state management**: `AuthContext` and `CartContext` handle global state
- **Route-based navigation**: React Router v7 with programmatic navigation 
- **Component organization**: Pages in `/pages`, reusable components in `/components`
- **API centralization**: Unified API layer in `/api` with separate admin endpoints

### Key Patterns

#### Authentication Flow
- Uses JWT tokens stored in localStorage
- `AuthContext` provides `user`, `login`, `logout` methods
- Supports 2FA verification flow in login process
- `ProtectedRoute` component wraps authenticated routes

#### API Communication  
- Centralized request handling in `api.js` with automatic token injection
- Separate `adminApi.js` for admin-specific endpoints
- Base URL: `http://localhost:8280/api/v1`
- Error handling with JSON error responses

#### State Management
- **Cart**: Global cart state via `CartContext` with methods like `addToCart`, `updateItem`, `removeItem`
- **Auth**: User authentication state with `useAuth()` hook
- **Local state**: Component-level state for UI interactions

#### Styling & UI
- **Tailwind CSS** for utility-first styling
- **Lucide React** icons throughout the application
- Custom CSS utilities in `index.css` (e.g., `.line-clamp-2`)
- Responsive grid layouts for product listings

## Development Workflows

### Running the Application
```bash
npm start     # Development server on localhost:3000
npm run build # Production build
npm test      # Run test suite
```

### Key File Patterns

#### Page Components
- Located in `/pages` directory
- Use hooks for data fetching and state management
- Accept navigation props or use `useNavigate()` hook

#### Product Management
- `ProductCard` component handles product display and interaction
- `ProductDetailsPage` shows detailed product info, reviews, and purchase options
- Products use image arrays with `isPrimary` flag for main image selection

#### Admin Interface
- Separate dashboard in `/pages/admin/AdminDashboard.js`
- Component-based navigation within admin panel
- Uses separate `adminApi.js` for backend communication

## Integration Points

### Backend API Expectations
- RESTful endpoints with pagination support (`page`, `size` params)
- Product details endpoint: `/products/{id}/details`
- Review system: `/products/{id}/reviews` with CRUD operations
- Cart operations: `/cart/items` with item management

### Navigation Patterns
- Programmatic navigation: `navigate('/product/123')`
- Route parameters: `/product/:id` pattern for product details
- Conditional header display based on current route

## Development Notes

### Adding New Features
- **New pages**: Add to `/pages`, register in `App.js` routes
- **New API endpoints**: Extend `api.js` or create specialized API files  
- **Global state**: Consider if it belongs in existing contexts or needs new context
- **Components**: Create in `/components` with prop-based configuration

### Common Gotchas
- Remember to pass `onViewDetails` prop to `ProductCard` components
- Use `useCallback` for functions in `useEffect` dependencies
- Import `useNavigate` when converting from prop-based navigation
- Admin routes require `requireAdmin={true}` in `ProtectedRoute`

### Error Handling
- API errors show user-friendly alerts
- Loading states with skeleton UI or spinners
- Fallback images for missing product photos
- Form validation with inline error messages